#!/bin/bash

###############################################################################
# CheckEat Restore Script
# Восстановление из бекапа PostgreSQL, Redis и файлов
###############################################################################

set -euo pipefail

# Цвета
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Функции логирования
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Конфигурация
BACKUP_DIR="${BACKUP_DIR:-/var/backups/checkeat}"
POSTGRES_CONTAINER="${POSTGRES_CONTAINER:-checkeat-postgres}"
REDIS_CONTAINER="${REDIS_CONTAINER:-checkeat-redis}"
BACKEND_CONTAINER="${BACKEND_CONTAINER:-checkeat-backend}"

DB_NAME="${DB_NAME:-checkeat}"
DB_USER="${DB_USER:-checkeat_user}"

# Проверка аргументов
usage() {
    echo "Использование: $0 [OPTIONS]"
    echo ""
    echo "Опции:"
    echo "  -t TIMESTAMP     Восстановить конкретный бекап (например: 20240115_143000)"
    echo "  -l              Показать список доступных бекапов"
    echo "  -p              Восстановить только PostgreSQL"
    echo "  -r              Восстановить только Redis"
    echo "  -f              Восстановить только файлы"
    echo "  -a              Восстановить всё (по умолчанию)"
    echo "  -y              Пропустить подтверждение"
    echo "  -h              Показать эту справку"
    echo ""
    echo "Примеры:"
    echo "  $0 -l                      # Список бекапов"
    echo "  $0 -t 20240115_143000      # Восстановить конкретный бекап"
    echo "  $0 -t latest -p            # Восстановить только PostgreSQL из последнего бекапа"
    exit 1
}

# Список доступных бекапов
list_backups() {
    log_info "Доступные бекапы:"
    echo ""

    if [ ! -d "$BACKUP_DIR" ] || [ -z "$(ls -A "$BACKUP_DIR"/backup_*.info 2>/dev/null)" ]; then
        log_warn "Бекапы не найдены в $BACKUP_DIR"
        exit 0
    fi

    for info_file in "$BACKUP_DIR"/backup_*.info; do
        timestamp=$(basename "$info_file" | sed 's/backup_//; s/.info//')
        echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
        echo -e "${BLUE}Timestamp: $timestamp${NC}"
        grep -E "^Date:|PostgreSQL:|Redis:|Files:|Total Backup Size:" "$info_file" | sed 's/^/  /'
        echo ""
    done

    exit 0
}

# Парсинг опций
TIMESTAMP=""
RESTORE_PG=false
RESTORE_REDIS=false
RESTORE_FILES=false
SKIP_CONFIRM=false

while getopts "t:lprfayh" opt; do
    case $opt in
        t) TIMESTAMP="$OPTARG" ;;
        l) list_backups ;;
        p) RESTORE_PG=true ;;
        r) RESTORE_REDIS=true ;;
        f) RESTORE_FILES=true ;;
        a) RESTORE_PG=true; RESTORE_REDIS=true; RESTORE_FILES=true ;;
        y) SKIP_CONFIRM=true ;;
        h) usage ;;
        *) usage ;;
    esac
done

# Если не указано что восстанавливать - восстанавливаем всё
if ! $RESTORE_PG && ! $RESTORE_REDIS && ! $RESTORE_FILES; then
    RESTORE_PG=true
    RESTORE_REDIS=true
    RESTORE_FILES=true
fi

# Определение последнего бекапа
if [ "$TIMESTAMP" = "latest" ] || [ -z "$TIMESTAMP" ]; then
    LATEST_INFO=$(ls -t "$BACKUP_DIR"/backup_*.info 2>/dev/null | head -1)
    if [ -z "$LATEST_INFO" ]; then
        log_error "Бекапы не найдены!"
        exit 1
    fi
    TIMESTAMP=$(basename "$LATEST_INFO" | sed 's/backup_//; s/.info//')
    log_info "Используется последний бекап: $TIMESTAMP"
fi

# Проверка существования бекапа
INFO_FILE="$BACKUP_DIR/backup_${TIMESTAMP}.info"
if [ ! -f "$INFO_FILE" ]; then
    log_error "Бекап $TIMESTAMP не найден!"
    log_info "Используйте -l для просмотра доступных бекапов"
    exit 1
fi

# Вывод информации о бекапе
log_info "Информация о бекапе:"
cat "$INFO_FILE"
echo ""

# Подтверждение
if ! $SKIP_CONFIRM; then
    log_warn "ВНИМАНИЕ! Эта операция перезапишет текущие данные!"
    read -p "Продолжить? (yes/no): " -r
    if [[ ! $REPLY =~ ^[Yy]es$ ]]; then
        log_info "Отменено пользователем"
        exit 0
    fi
fi

###############################################################################
# ВОССТАНОВЛЕНИЕ POSTGRESQL
###############################################################################

if $RESTORE_PG; then
    log_step "Восстановление PostgreSQL..."

    PG_BACKUP="$BACKUP_DIR/postgresql/checkeat_${TIMESTAMP}.sql.gz"

    if [ ! -f "$PG_BACKUP" ]; then
        log_error "Файл бекапа PostgreSQL не найден: $PG_BACKUP"
        exit 1
    fi

    # Проверка MD5
    if [ -f "$PG_BACKUP.md5" ]; then
        log_info "Проверка контрольной суммы..."
        if md5sum -c "$PG_BACKUP.md5" > /dev/null 2>&1; then
            log_info "✓ Контрольная сумма совпадает"
        else
            log_error "✗ Контрольная сумма не совпадает! Файл поврежден!"
            exit 1
        fi
    fi

    log_info "Остановка backend..."
    docker-compose stop backend || true

    log_info "Удаление текущей базы данных..."
    docker exec "$POSTGRES_CONTAINER" psql -U "$DB_USER" -c "DROP DATABASE IF EXISTS $DB_NAME;" postgres

    log_info "Создание новой базы данных..."
    docker exec "$POSTGRES_CONTAINER" psql -U "$DB_USER" -c "CREATE DATABASE $DB_NAME;" postgres

    log_info "Восстановление из бекапа..."
    gunzip -c "$PG_BACKUP" | docker exec -i "$POSTGRES_CONTAINER" psql -U "$DB_USER" "$DB_NAME"

    log_info "✓ PostgreSQL восстановлен успешно"
fi

###############################################################################
# ВОССТАНОВЛЕНИЕ REDIS
###############################################################################

if $RESTORE_REDIS; then
    log_step "Восстановление Redis..."

    REDIS_BACKUP="$BACKUP_DIR/redis/redis_${TIMESTAMP}.rdb.gz"

    if [ ! -f "$REDIS_BACKUP" ]; then
        log_warn "Файл бекапа Redis не найден: $REDIS_BACKUP (пропуск)"
    else
        log_info "Остановка Redis..."
        docker-compose stop redis

        log_info "Восстановление dump.rdb..."
        gunzip -c "$REDIS_BACKUP" | docker cp - "$REDIS_CONTAINER:/data/dump.rdb"

        log_info "Запуск Redis..."
        docker-compose start redis

        # Ожидание запуска
        sleep 3

        if docker exec "$REDIS_CONTAINER" redis-cli PING | grep -q PONG; then
            log_info "✓ Redis восстановлен успешно"
        else
            log_error "✗ Ошибка запуска Redis"
            exit 1
        fi
    fi
fi

###############################################################################
# ВОССТАНОВЛЕНИЕ ФАЙЛОВ
###############################################################################

if $RESTORE_FILES; then
    log_step "Восстановление файлов..."

    FILES_BACKUP="$BACKUP_DIR/files/uploads_${TIMESTAMP}.tar.gz"

    if [ ! -f "$FILES_BACKUP" ]; then
        log_warn "Файл бекапа uploads не найден: $FILES_BACKUP (пропуск)"
    else
        log_info "Удаление старых файлов..."
        docker exec "$BACKEND_CONTAINER" rm -rf /app/uploads/* || true

        log_info "Восстановление файлов..."
        docker cp "$FILES_BACKUP" "$BACKEND_CONTAINER:/tmp/uploads.tar.gz"
        docker exec "$BACKEND_CONTAINER" tar xzf /tmp/uploads.tar.gz -C /
        docker exec "$BACKEND_CONTAINER" rm /tmp/uploads.tar.gz

        log_info "✓ Файлы восстановлены успешно"
    fi
fi

###############################################################################
# ФИНАЛИЗАЦИЯ
###############################################################################

log_step "Перезапуск сервисов..."
docker-compose restart

log_info "Ожидание запуска сервисов..."
sleep 5

# Проверка здоровья
if curl -f http://localhost:8000/health > /dev/null 2>&1; then
    log_info "✓ Backend работает корректно"
else
    log_warn "Backend не отвечает на /health endpoint"
fi

log_info "════════════════════════════════════════"
log_info "✓ Восстановление завершено успешно!"
log_info "  Timestamp: $TIMESTAMP"
log_info "  PostgreSQL: $(${RESTORE_PG} && echo '✓' || echo '○')"
log_info "  Redis:      $(${RESTORE_REDIS} && echo '✓' || echo '○')"
log_info "  Files:      $(${RESTORE_FILES} && echo '✓' || echo '○')"
log_info "════════════════════════════════════════"

exit 0
