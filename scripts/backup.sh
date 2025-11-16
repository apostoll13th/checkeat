#!/bin/bash

###############################################################################
# CheckEat Backup Script
# Создает полный бекап PostgreSQL, Redis и загруженных файлов
###############################################################################

set -euo pipefail

# Конфигурация
BACKUP_DIR="${BACKUP_DIR:-/var/backups/checkeat}"
RETENTION_DAYS="${RETENTION_DAYS:-30}"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
DATE=$(date +"%Y-%m-%d %H:%M:%S")

# Docker контейнеры
POSTGRES_CONTAINER="${POSTGRES_CONTAINER:-checkeat-postgres}"
REDIS_CONTAINER="${REDIS_CONTAINER:-checkeat-redis}"
BACKEND_CONTAINER="${BACKEND_CONTAINER:-checkeat-backend}"

# База данных
DB_NAME="${DB_NAME:-checkeat}"
DB_USER="${DB_USER:-checkeat_user}"

# Цвета для логов
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Функции логирования
log_info() {
    echo -e "${GREEN}[INFO]${NC} $DATE - $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $DATE - $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $DATE - $1"
}

# Проверка наличия Docker
if ! command -v docker &> /dev/null; then
    log_error "Docker не установлен!"
    exit 1
fi

# Создание директорий для бекапов
mkdir -p "$BACKUP_DIR"/{postgresql,redis,files}

log_info "Начало процесса резервного копирования..."

###############################################################################
# 1. БЕКАП POSTGRESQL
###############################################################################

log_info "Создание бекапа PostgreSQL..."

POSTGRES_BACKUP="$BACKUP_DIR/postgresql/checkeat_${TIMESTAMP}.sql.gz"

if docker exec "$POSTGRES_CONTAINER" pg_dump -U "$DB_USER" "$DB_NAME" | gzip > "$POSTGRES_BACKUP"; then
    BACKUP_SIZE=$(du -h "$POSTGRES_BACKUP" | cut -f1)
    log_info "✓ PostgreSQL бекап создан: $POSTGRES_BACKUP ($BACKUP_SIZE)"
else
    log_error "✗ Ошибка создания бекапа PostgreSQL"
    exit 1
fi

# Создание контрольной суммы
md5sum "$POSTGRES_BACKUP" > "$POSTGRES_BACKUP.md5"

###############################################################################
# 2. БЕКАП REDIS
###############################################################################

log_info "Создание бекапа Redis..."

REDIS_BACKUP="$BACKUP_DIR/redis/redis_${TIMESTAMP}.rdb"

# Триггерим SAVE в Redis
if docker exec "$REDIS_CONTAINER" redis-cli SAVE > /dev/null; then
    # Копируем dump.rdb из контейнера
    if docker cp "$REDIS_CONTAINER:/data/dump.rdb" "$REDIS_BACKUP"; then
        BACKUP_SIZE=$(du -h "$REDIS_BACKUP" | cut -f1)
        log_info "✓ Redis бекап создан: $REDIS_BACKUP ($BACKUP_SIZE)"

        # Сжимаем
        gzip -f "$REDIS_BACKUP"
        log_info "✓ Redis бекап сжат: ${REDIS_BACKUP}.gz"
    else
        log_warn "✗ Не удалось скопировать dump.rdb из контейнера"
    fi
else
    log_warn "✗ Ошибка выполнения SAVE в Redis"
fi

###############################################################################
# 3. БЕКАП ФАЙЛОВ (загруженные фото еды)
###############################################################################

log_info "Создание бекапа загруженных файлов..."

FILES_BACKUP="$BACKUP_DIR/files/uploads_${TIMESTAMP}.tar.gz"

# Путь к uploads внутри контейнера
UPLOADS_PATH="/app/uploads"

if docker exec "$BACKEND_CONTAINER" tar czf - "$UPLOADS_PATH" > "$FILES_BACKUP" 2>/dev/null; then
    BACKUP_SIZE=$(du -h "$FILES_BACKUP" | cut -f1)
    log_info "✓ Файлы сохранены: $FILES_BACKUP ($BACKUP_SIZE)"
else
    log_warn "✗ Не удалось создать бекап файлов (возможно директория пуста)"
fi

###############################################################################
# 4. СОЗДАНИЕ МЕТА-ИНФОРМАЦИИ
###############################################################################

log_info "Создание метаданных бекапа..."

cat > "$BACKUP_DIR/backup_${TIMESTAMP}.info" <<EOF
CheckEat Backup Information
===========================
Date: $DATE
Timestamp: $TIMESTAMP

PostgreSQL:
  Container: $POSTGRES_CONTAINER
  Database: $DB_NAME
  File: $(basename "$POSTGRES_BACKUP")
  Size: $(du -h "$POSTGRES_BACKUP" | cut -f1)
  MD5: $(cat "$POSTGRES_BACKUP.md5" | cut -d' ' -f1)

Redis:
  Container: $REDIS_CONTAINER
  File: redis_${TIMESTAMP}.rdb.gz
  Size: $([ -f "${REDIS_BACKUP}.gz" ] && du -h "${REDIS_BACKUP}.gz" | cut -f1 || echo "N/A")

Files:
  Container: $BACKEND_CONTAINER
  File: $(basename "$FILES_BACKUP")
  Size: $([ -f "$FILES_BACKUP" ] && du -h "$FILES_BACKUP" | cut -f1 || echo "N/A")

Total Backup Size: $(du -sh "$BACKUP_DIR" | cut -f1)
EOF

log_info "✓ Метаданные сохранены"

###############################################################################
# 5. РОТАЦИЯ СТАРЫХ БЕКАПОВ
###############################################################################

log_info "Удаление бекапов старше $RETENTION_DAYS дней..."

DELETED_COUNT=0

for dir in postgresql redis files; do
    while IFS= read -r -d '' file; do
        rm -f "$file"
        ((DELETED_COUNT++))
    done < <(find "$BACKUP_DIR/$dir" -type f -mtime +"$RETENTION_DAYS" -print0)
done

# Удаление старых .info файлов
while IFS= read -r -d '' file; do
    rm -f "$file"
    ((DELETED_COUNT++))
done < <(find "$BACKUP_DIR" -maxdepth 1 -name "backup_*.info" -mtime +"$RETENTION_DAYS" -print0)

if [ "$DELETED_COUNT" -gt 0 ]; then
    log_info "✓ Удалено $DELETED_COUNT старых файлов"
else
    log_info "✓ Старых бекапов для удаления не найдено"
fi

###############################################################################
# 6. ОТПРАВКА В ОБЛАКО (опционально)
###############################################################################

if [ -n "${AWS_S3_BUCKET:-}" ]; then
    log_info "Загрузка бекапов в S3..."

    if command -v aws &> /dev/null; then
        aws s3 sync "$BACKUP_DIR" "s3://$AWS_S3_BUCKET/checkeat-backups/" \
            --exclude "*" \
            --include "backup_${TIMESTAMP}.info" \
            --include "postgresql/checkeat_${TIMESTAMP}.sql.gz*" \
            --include "redis/redis_${TIMESTAMP}.rdb.gz" \
            --include "files/uploads_${TIMESTAMP}.tar.gz"

        log_info "✓ Бекапы загружены в S3"
    else
        log_warn "AWS CLI не установлен, пропуск загрузки в S3"
    fi
fi

###############################################################################
# 7. ФИНАЛЬНАЯ СТАТИСТИКА
###############################################################################

TOTAL_SIZE=$(du -sh "$BACKUP_DIR" | cut -f1)
BACKUP_COUNT=$(find "$BACKUP_DIR" -type f -name "backup_*.info" | wc -l)

log_info "════════════════════════════════════════"
log_info "✓ Резервное копирование завершено!"
log_info "  Всего бекапов: $BACKUP_COUNT"
log_info "  Общий размер: $TOTAL_SIZE"
log_info "  Директория: $BACKUP_DIR"
log_info "════════════════════════════════════════"

exit 0
