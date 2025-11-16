# CheckEat Makefile - удобные команды для работы с проектом

.PHONY: help up down restart logs build clean test android-build

# Показать помощь
help:
	@echo "CheckEat - Доступные команды:"
	@echo ""
	@echo "  make up              - Запустить все сервисы (backend, postgres, redis)"
	@echo "  make down            - Остановить все сервисы"
	@echo "  make restart         - Перезапустить все сервисы"
	@echo "  make logs            - Показать логи всех сервисов"
	@echo "  make logs-backend    - Показать логи backend"
	@echo "  make build           - Пересобрать все образы"
	@echo "  make clean           - Остановить и удалить все (включая volumes)"
	@echo "  make test            - Запустить тесты backend"
	@echo "  make android-build   - Собрать Android APK"
	@echo "  make android-release - Собрать Release APK"
	@echo "  make shell           - Зайти в shell backend контейнера"
	@echo "  make db-migrate      - Применить миграции базы данных"
	@echo "  make db-shell        - Подключиться к PostgreSQL"
	@echo ""

# Запустить все сервисы
up:
	docker compose up -d

# Остановить все сервисы
down:
	docker compose down

# Перезапустить все сервисы
restart:
	docker compose restart

# Показать логи
logs:
	docker compose logs -f

# Логи backend
logs-backend:
	docker compose logs -f backend

# Пересобрать образы
build:
	docker compose build --no-cache

# Остановить и удалить все
clean:
	docker compose down -v
	@echo "⚠️  Все данные удалены!"

# Запустить тесты
test:
	docker compose exec backend pytest

# Собрать Android APK
android-build:
	@echo "🔨 Сборка Android APK..."
	docker compose --profile build run --rm android-builder
	@echo "✅ APK готов: android/app/build/outputs/apk/debug/app-debug.apk"

# Собрать Release APK
android-release:
	@echo "🔨 Сборка Release APK..."
	docker compose --profile build run --rm android-builder ./gradlew assembleRelease --no-daemon
	@echo "✅ Release APK готов: android/app/build/outputs/apk/release/app-release.apk"

# Shell в backend контейнере
shell:
	docker compose exec backend bash

# Применить миграции
db-migrate:
	docker compose exec backend alembic upgrade head

# Создать новую миграцию
db-revision:
	@read -p "Введите название миграции: " name; \
	docker compose exec backend alembic revision --autogenerate -m "$$name"

# Shell PostgreSQL
db-shell:
	docker compose exec postgres psql -U checkeat_user -d checkeat_db

# Проверка статуса сервисов
status:
	docker compose ps

# Форматирование кода backend
format:
	docker compose exec backend black app/
	docker compose exec backend isort app/

# Линтер
lint:
	docker compose exec backend flake8 app/

# Полная проверка кода
check: format lint test
	@echo "✅ Все проверки пройдены!"

# Production запуск
prod-up:
	docker compose -f docker-compose.prod.yml up -d

# Production остановка
prod-down:
	docker compose -f docker-compose.prod.yml down

# Production логи
prod-logs:
	docker compose -f docker-compose.prod.yml logs -f
