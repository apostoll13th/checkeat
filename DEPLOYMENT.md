# CheckEat Deployment Guide

Полное руководство по развертыванию CheckEat в production.

## 📋 Требования

- **Сервер**: Ubuntu 20.04+ / Debian 11+
- **CPU**: 2+ cores
- **RAM**: 4GB+ (рекомендуется 8GB)
- **Disk**: 50GB+ SSD
- **Docker**: 24.0+
- **Docker Compose**: 2.0+
- **Домен**: с настроенными DNS записями

## 🚀 Быстрый старт

### 1. Подготовка сервера

```bash
# Обновление системы
sudo apt update && sudo apt upgrade -y

# Установка Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Установка Docker Compose
sudo apt install docker-compose-plugin -y

# Добавление пользователя в группу docker
sudo usermod -aG docker $USER
newgrp docker

# Установка дополнительных утилит
sudo apt install -y git curl wget htop

# Создание директории приложения
sudo mkdir -p /opt/checkeat
sudo chown $USER:$USER /opt/checkeat
```

### 2. Клонирование репозитория

```bash
cd /opt/checkeat
git clone https://github.com/yourusername/checkeat.git .
```

### 3. Настройка переменных окружения

```bash
# Копирование примера конфигурации
cp .env.production.example .env.production

# Редактирование конфигурации
nano .env.production
```

**Обязательно измените:**
- `ANTHROPIC_API_KEY` - ваш API ключ от Anthropic
- `DB_PASSWORD` - сильный пароль для PostgreSQL
- `REDIS_PASSWORD` - сильный пароль для Redis
- `SECRET_KEY` - случайная строка 64 символа
- `JWT_SECRET_KEY` - случайная строка 64 символа
- `CORS_ORIGINS` - ваши домены

**Генерация секретных ключей:**
```bash
# SECRET_KEY
openssl rand -hex 32

# JWT_SECRET_KEY
openssl rand -hex 32
```

### 4. Настройка SSL (Let's Encrypt)

```bash
# Установка Certbot
sudo apt install -y certbot python3-certbot-nginx

# Получение сертификата
sudo certbot certonly --standalone -d api.yourdomain.com

# Сертификаты будут в:
# /etc/letsencrypt/live/api.yourdomain.com/
```

### 5. Запуск приложения

```bash
# Загрузка переменных окружения
export $(cat .env.production | grep -v '^#' | xargs)

# Запуск сервисов
docker-compose -f docker-compose.prod.yml up -d

# Проверка статуса
docker-compose -f docker-compose.prod.yml ps

# Просмотр логов
docker-compose -f docker-compose.prod.yml logs -f
```

### 6. Применение миграций базы данных

```bash
# Выполнение миграций
docker-compose -f docker-compose.prod.yml exec backend alembic upgrade head

# Создание первого пользователя (опционально)
docker-compose -f docker-compose.prod.yml exec backend python -c "
from app.db.database import SessionLocal
from app.models.user import User
from app.core.security import get_password_hash

db = SessionLocal()
user = User(
    email='admin@checkeat.com',
    hashed_password=get_password_hash('admin123'),
    full_name='Admin'
)
db.add(user)
db.commit()
print('Admin user created!')
"
```

## 🔒 Безопасность

### Firewall

```bash
# UFW
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw enable
```

### Fail2ban

```bash
sudo apt install -y fail2ban
sudo systemctl enable fail2ban
sudo systemctl start fail2ban
```

## 💾 Настройка автоматических бекапов

### Установка

```bash
cd /opt/checkeat

# Настройка cron задач для автоматических бекапов
sudo bash scripts/setup-backups.sh
```

Это создаст:
- Ежедневный бекап в 03:00
- Еженедельный полный бекап в воскресенье в 04:00

### Ручной бекап

```bash
# Создание бекапа
sudo bash scripts/backup.sh

# Просмотр бекапов
sudo bash scripts/restore.sh -l

# Восстановление конкретного бекапа
sudo bash scripts/restore.sh -t 20240115_143000

# Восстановление последнего бекапа
sudo bash scripts/restore.sh -t latest
```

### Бекапы в S3 (опционально)

```bash
# Установка AWS CLI
sudo apt install -y awscli

# Настройка
aws configure

# Добавьте в .env.production
echo "AWS_S3_BUCKET=your-bucket-name" >> .env.production

# Бекапы автоматически будут загружаться в S3
```

## 📊 Мониторинг

### Проверка здоровья

```bash
# API health check
curl http://localhost:8000/health

# Статус контейнеров
docker-compose -f docker-compose.prod.yml ps

# Использование ресурсов
docker stats
```

### Логи

```bash
# Все логи
docker-compose -f docker-compose.prod.yml logs -f

# Только backend
docker-compose -f docker-compose.prod.yml logs -f backend

# Последние 100 строк
docker-compose -f docker-compose.prod.yml logs --tail=100 backend

# Логи бекапов
tail -f /var/log/checkeat-backup.log
```

## 🔄 Обновление приложения

```bash
cd /opt/checkeat

# Создание бекапа перед обновлением
sudo bash scripts/backup.sh

# Получение последних изменений
git pull origin main

# Пересборка и перезапуск
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d --no-deps backend

# Применение миграций
docker-compose -f docker-compose.prod.yml exec backend alembic upgrade head

# Проверка
curl http://localhost:8000/health
```

## 🔧 CI/CD (автоматический деплой)

### GitHub Actions Secrets

Добавьте в Settings → Secrets and variables → Actions:

- `DOCKERHUB_USERNAME` - имя пользователя Docker Hub
- `DOCKERHUB_TOKEN` - токен Docker Hub
- `DEPLOY_HOST` - IP вашего сервера
- `DEPLOY_USER` - SSH пользователь
- `DEPLOY_SSH_KEY` - приватный SSH ключ
- `API_URL` - URL вашего API (для health check)

### SSH ключ для деплоя

```bash
# На локальной машине
ssh-keygen -t ed25519 -C "github-actions" -f ~/.ssh/checkeat_deploy

# Добавьте публичный ключ на сервер
ssh-copy-id -i ~/.ssh/checkeat_deploy.pub user@your-server

# Приватный ключ добавьте в GitHub Secrets как DEPLOY_SSH_KEY
cat ~/.ssh/checkeat_deploy
```

### Автоматический деплой

После настройки каждый push в `main` будет:
1. Собирать и тестировать код
2. Создавать Docker образ
3. Загружать в Docker Hub
4. Деплоить на сервер
5. Выполнять health check

## 🐛 Решение проблем

### Backend не запускается

```bash
# Проверка логов
docker-compose -f docker-compose.prod.yml logs backend

# Проверка переменных окружения
docker-compose -f docker-compose.prod.yml exec backend env

# Пересоздание контейнера
docker-compose -f docker-compose.prod.yml up -d --force-recreate backend
```

### PostgreSQL проблемы

```bash
# Подключение к базе
docker-compose -f docker-compose.prod.yml exec postgres psql -U checkeat_user -d checkeat

# Проверка соединений
docker-compose -f docker-compose.prod.yml exec postgres psql -U checkeat_user -c "SELECT * FROM pg_stat_activity;"
```

### Ошибки миграций

```bash
# Откат последней миграции
docker-compose -f docker-compose.prod.yml exec backend alembic downgrade -1

# Повторное применение
docker-compose -f docker-compose.prod.yml exec backend alembic upgrade head

# История миграций
docker-compose -f docker-compose.prod.yml exec backend alembic history
```

## 📱 Сборка Android APK

### Локально

```bash
cd android

# Debug сборка
./gradlew assembleDebug

# Release сборка
./gradlew assembleRelease

# APK будет в: app/build/outputs/apk/
```

### GitHub Actions

При push с тегом (`git tag v1.0.0 && git push --tags`):
- Автоматически соберется signed APK
- Создастся GitHub Release
- APK будет прикреплен к релизу

## 🎯 Production Checklist

- [ ] Изменены все пароли в `.env.production`
- [ ] Настроен SSL сертификат
- [ ] Настроен firewall
- [ ] Установлен fail2ban
- [ ] Настроены автоматические бекапы
- [ ] Протестирован процесс восстановления
- [ ] Настроен мониторинг
- [ ] Настроены GitHub Actions secrets
- [ ] Проверен health endpoint
- [ ] Настроены email уведомления (опционально)
- [ ] Настроен Sentry для отслеживания ошибок (опционально)

## 📞 Поддержка

При возникновении проблем:
1. Проверьте логи: `docker-compose logs`
2. Проверьте health: `curl http://localhost:8000/health`
3. Создайте issue на GitHub

---

**Удачного деплоя! 🚀**
