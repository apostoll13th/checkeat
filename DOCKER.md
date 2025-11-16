# CheckEat Docker Guide

Полное руководство по запуску CheckEat через Docker Compose.

## 📋 Содержание

- [Требования](#требования)
- [Быстрый старт](#быстрый-старт)
- [Сервисы](#сервисы)
- [Команды](#команды)
- [Сборка Android APK](#сборка-android-apk)
- [Разработка](#разработка)
- [Troubleshooting](#troubleshooting)

---

## 🔧 Требования

- **Docker** >= 20.10
- **Docker Compose** >= 2.0
- **Git**
- Минимум 4 GB RAM
- Минимум 10 GB свободного места

Проверьте установку:
```bash
docker --version
docker-compose --version
```

---

## 🚀 Быстрый старт

### 1. Клонируйте репозиторий

```bash
git clone <repository-url>
cd checkeat
```

### 2. Настройте переменные окружения

```bash
# Скопируйте пример конфигурации
cp .env.example .env

# Отредактируйте .env и добавьте ваш Anthropic API ключ
nano .env
```

**Обязательно** добавьте в `.env`:
```env
ANTHROPIC_API_KEY=your_actual_api_key_here
JWT_SECRET_KEY=$(openssl rand -hex 32)
SECRET_KEY=$(openssl rand -hex 32)
```

### 3. Запустите backend сервисы

```bash
# Запуск в фоновом режиме
docker-compose up -d

# Или с логами в реальном времени
docker-compose up
```

### 4. Проверьте статус

```bash
# Проверить статус всех сервисов
docker-compose ps

# Посмотреть логи
docker-compose logs -f backend
```

Backend будет доступен на: **http://localhost:8000**

API документация: **http://localhost:8000/docs**

---

## 📦 Сервисы

### PostgreSQL (База данных)
- **Порт**: 5432
- **Контейнер**: `checkeat_postgres`
- **Данные**: хранятся в volume `postgres_data`

### Redis (Кэш)
- **Порт**: 6379
- **Контейнер**: `checkeat_redis`
- **Данные**: хранятся в volume `redis_data`

### Backend API
- **Порт**: 8000
- **Контейнер**: `checkeat_backend`
- **Hot reload**: включен (изменения кода применяются автоматически)
- **Загрузки**: хранятся в volume `uploads_data`

### Android Builder
- **Контейнер**: `checkeat_android_builder`
- **Profile**: `build` (запускается по требованию)
- **Выход**: APK файлы в `android/app/build/outputs/apk/`

---

## 🎯 Команды

### Управление сервисами

```bash
# Запустить все сервисы
docker-compose up -d

# Остановить все сервисы
docker-compose down

# Остановить и удалить volumes (УДАЛИТ ВСЕ ДАННЫЕ!)
docker-compose down -v

# Перезапустить сервис
docker-compose restart backend

# Пересобрать и запустить
docker-compose up -d --build
```

### Логи

```bash
# Все логи
docker-compose logs

# Логи конкретного сервиса
docker-compose logs backend

# Следить за логами в реальном времени
docker-compose logs -f backend

# Последние 100 строк
docker-compose logs --tail=100 backend
```

### Выполнение команд внутри контейнеров

```bash
# Зайти в shell backend контейнера
docker-compose exec backend bash

# Выполнить миграции базы данных
docker-compose exec backend alembic upgrade head

# Создать нового пользователя
docker-compose exec backend python -m app.scripts.create_user

# Запустить тесты
docker-compose exec backend pytest

# Подключиться к PostgreSQL
docker-compose exec postgres psql -U checkeat_user -d checkeat_db

# Подключиться к Redis CLI
docker-compose exec redis redis-cli
```

---

## 📱 Сборка Android APK

### Собрать Debug APK

```bash
# Собрать APK через Docker
docker-compose --profile build run --rm android-builder

# APK будет в android/app/build/outputs/apk/debug/
ls -lh android/app/build/outputs/apk/debug/
```

### Собрать Release APK

```bash
# Release APK (требуется подписание)
docker-compose --profile build run --rm android-builder ./gradlew assembleRelease --no-daemon
```

### Скопировать APK на хост

APK автоматически доступен в:
```
android/app/build/outputs/apk/debug/app-debug.apk
android/app/build/outputs/apk/release/app-release.apk
```

### Установить APK на устройство

```bash
# Через adb (устройство должно быть подключено)
adb install android/app/build/outputs/apk/debug/app-debug.apk
```

---

## 💻 Разработка

### Hot Reload

Backend автоматически перезагружается при изменении кода:
```bash
# Просто редактируйте файлы в backend/
nano backend/app/api/v1/food_analysis.py

# Uvicorn автоматически перезапустится
```

### Работа с базой данных

```bash
# Создать новую миграцию
docker-compose exec backend alembic revision --autogenerate -m "Add new table"

# Применить миграции
docker-compose exec backend alembic upgrade head

# Откатить миграцию
docker-compose exec backend alembic downgrade -1

# Посмотреть историю миграций
docker-compose exec backend alembic history
```

### Тестирование

```bash
# Запустить все тесты
docker-compose exec backend pytest

# Тесты с покрытием
docker-compose exec backend pytest --cov=app --cov-report=html

# Запустить конкретный тест
docker-compose exec backend pytest tests/test_food_analysis.py
```

### Проверка кода

```bash
# Форматирование с Black
docker-compose exec backend black app/

# Сортировка импортов
docker-compose exec backend isort app/

# Линтер Flake8
docker-compose exec backend flake8 app/
```

---

## 🔍 Troubleshooting

### Backend не запускается

```bash
# Проверьте логи
docker-compose logs backend

# Проверьте health status
docker-compose ps

# Пересоздайте контейнер
docker-compose up -d --force-recreate backend
```

### База данных не подключается

```bash
# Проверьте что PostgreSQL запущен
docker-compose ps postgres

# Проверьте логи PostgreSQL
docker-compose logs postgres

# Проверьте подключение вручную
docker-compose exec postgres psql -U checkeat_user -d checkeat_db -c "SELECT 1;"
```

### Ошибки с Redis

```bash
# Проверьте статус Redis
docker-compose exec redis redis-cli ping

# Должно вывести: PONG
```

### Android сборка падает

```bash
# Очистите Gradle кэш
docker-compose --profile build run --rm android-builder ./gradlew clean

# Пересоберите Docker образ
docker-compose --profile build build android-builder

# Проверьте логи сборки
docker-compose --profile build run --rm android-builder ./gradlew assembleDebug --stacktrace
```

### Нехватка памяти

```bash
# Очистите неиспользуемые образы и контейнеры
docker system prune -a

# Очистите volumes (ОСТОРОЖНО: удалит данные!)
docker volume prune
```

### Порты заняты

```bash
# Проверьте что занимает порт 8000
lsof -i :8000
netstat -tulpn | grep 8000

# Измените порт в docker-compose.yml
# ports:
#   - "8001:8000"  # Используйте 8001 вместо 8000
```

---

## 🔐 Безопасность

### Production

Для production используйте `docker-compose.prod.yml`:

```bash
# Настройте production переменные
cp .env.production.example .env.production
nano .env.production

# Запустите в production режиме
docker-compose -f docker-compose.prod.yml up -d
```

**Важно для production:**
- Измените все секретные ключи
- Установите `DEBUG=False`
- Используйте сильные пароли для PostgreSQL
- Настройте SSL/TLS для HTTPS
- Настройте firewall
- Включите регулярные бекапы

---

## 📊 Мониторинг

### Проверка здоровья сервисов

```bash
# Health check backend
curl http://localhost:8000/health

# Статус всех контейнеров
docker-compose ps

# Использование ресурсов
docker stats
```

### Бекапы

```bash
# Создать бекап (требуется настройка скриптов)
./scripts/backup.sh

# Восстановить из бекапа
./scripts/restore.sh
```

---

## 📚 Дополнительные ресурсы

- [Backend API Docs](http://localhost:8000/docs) - Swagger UI
- [ReDoc API](http://localhost:8000/redoc) - Alternative API docs
- [DEPLOYMENT.md](./DEPLOYMENT.md) - Production deployment guide
- [README.md](./README.md) - Project overview

---

## 🆘 Получение помощи

Если у вас возникли проблемы:

1. Проверьте логи: `docker-compose logs -f`
2. Проверьте статус: `docker-compose ps`
3. Прочитайте секцию Troubleshooting выше
4. Создайте Issue в репозитории

---

**Версия**: 1.0
**Последнее обновление**: 2024
