#!/bin/bash

###############################################################################
# CheckEat Backup Setup Script
# Настройка автоматических бекапов через cron
###############################################################################

set -euo pipefail

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# Проверка прав root
if [ "$EUID" -ne 0 ]; then
    log_warn "Этот скрипт требует права root для настройки cron"
    log_warn "Запустите: sudo $0"
    exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_SCRIPT="$SCRIPT_DIR/backup.sh"

if [ ! -f "$BACKUP_SCRIPT" ]; then
    echo "Ошибка: backup.sh не найден в $SCRIPT_DIR"
    exit 1
fi

# Делаем скрипты исполняемыми
chmod +x "$BACKUP_SCRIPT"
chmod +x "$SCRIPT_DIR/restore.sh"

log_info "Скрипты бекапов сделаны исполняемыми"

# Настройка cron
log_info "Настройка автоматических бекапов..."

cat > /etc/cron.d/checkeat-backup <<EOF
# CheckEat Automatic Backups
SHELL=/bin/bash
PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin

# Ежедневный бекап в 3:00 AM
0 3 * * * root $BACKUP_SCRIPT >> /var/log/checkeat-backup.log 2>&1

# Еженедельный полный бекап в воскресенье в 4:00 AM
0 4 * * 0 root RETENTION_DAYS=90 $BACKUP_SCRIPT >> /var/log/checkeat-backup-weekly.log 2>&1
EOF

chmod 644 /etc/cron.d/checkeat-backup

log_info "✓ Cron задачи настроены:"
log_info "  - Ежедневный бекап: 03:00"
log_info "  - Еженедельный бекап: Воскресенье 04:00"

# Создание лог-файлов
touch /var/log/checkeat-backup.log
touch /var/log/checkeat-backup-weekly.log
chmod 644 /var/log/checkeat-backup*.log

log_info "✓ Лог-файлы созданы"

# Настройка logrotate
log_info "Настройка ротации логов..."

cat > /etc/logrotate.d/checkeat-backup <<EOF
/var/log/checkeat-backup*.log {
    weekly
    rotate 12
    compress
    delaycompress
    missingok
    notifempty
    create 0644 root root
}
EOF

log_info "✓ Logrotate настроен"

# Тестовый запуск
log_info "Запуск тестового бекапа..."
if $BACKUP_SCRIPT; then
    log_info "✓ Тестовый бекап выполнен успешно!"
else
    log_warn "✗ Ошибка выполнения тестового бекапа"
fi

log_info "════════════════════════════════════════"
log_info "✓ Настройка завершена!"
log_info ""
log_info "Команды:"
log_info "  Ручной бекап:     $BACKUP_SCRIPT"
log_info "  Восстановление:   $SCRIPT_DIR/restore.sh -l"
log_info "  Просмотр логов:   tail -f /var/log/checkeat-backup.log"
log_info "════════════════════════════════════════"

exit 0
