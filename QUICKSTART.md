# CheckEat - Быстрый старт

Минимально необходимые шаги для запуска CheckEat с первого раза без ошибок.

## 🚀 Запуск за 5 минут

### 1. Запуск Backend

```bash
# Клонируйте репозиторий (если еще не сделано)
git clone <repository-url>
cd checkeat

# Создайте .env файл
cp .env.example .env

# Добавьте ваш Anthropic API ключ в .env
nano .env
# или
echo "ANTHROPIC_API_KEY=sk-ant-your-key-here" >> .env

# Запустите backend через Docker
docker compose up -d

# Проверьте что всё работает
curl http://localhost:8000/health
# Должно вернуть: {"status":"healthy"}
```

**API документация**: http://localhost:8000/docs

**Важно:** Убедитесь что Docker Desktop запущен!

### 2. Тестирование API

```bash
# Регистрация пользователя
curl -X POST http://localhost:8000/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "test123",
    "name": "Test User"
  }'

# Сохраните полученный access_token

# Анализ фото (замените YOUR_TOKEN и path/to/image.jpg)
curl -X POST http://localhost:8000/api/v1/analyze \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@path/to/image.jpg"
```

### 3. Запуск Android приложения

#### Вариант A: В Android Studio

```bash
# 1. Откройте Android Studio
# 2. File -> Open -> выберите папку android/
# 3. Дождитесь синхронизации Gradle (5-10 минут при первом запуске)
# 4. Нажмите Run (зелёная кнопка play)
# 5. Выберите эмулятор

# API URL уже настроен для эмулятора: http://10.0.2.2:8000
```

#### Вариант B: Собрать APK через Docker

```bash
# Debug APK
make android-build

# APK будет в:
# android/app/build/outputs/apk/dev/debug/app-dev-debug.apk

# Установить на устройство
adb install android/app/build/outputs/apk/dev/debug/app-dev-debug.apk
```

#### Для реального устройства (Samsung S25 Ultra и др.)

```bash
# 1. Узнайте IP компьютера
ifconfig | grep inet  # Mac/Linux
ipconfig              # Windows

# 2. Отредактируйте android/app/build.gradle.kts (строка 59):
# buildConfigField("String", "API_BASE_URL", "\"http://YOUR_IP:8000/api/v1/\"")

# 3. Соберите
cd android && ./gradlew assembleDevDebug

# 4. Установите
adb install app/build/outputs/apk/dev/debug/app-dev-debug.apk
```

**Подробная инструкция:** [SAMSUNG_S25_ULTRA_SETUP.md](./SAMSUNG_S25_ULTRA_SETUP.md)

## ✅ Что реализовано

### Backend (100% готов)
- ✅ Регистрация и вход (JWT)
- ✅ Анализ фото через Claude Vision AI
- ✅ История анализов с фильтрами
- ✅ Система заметок (CRUD + поиск)
- ✅ Статистика (день/неделя/месяц)
- ✅ Экспорт данных (CSV)
- ✅ Docker контейнеризация
- ✅ CI/CD pipeline
- ✅ Система бекапов

### Android (Production-ready структура)
- ✅ Jetpack Compose + Material Design 3
- ✅ Clean Architecture (Data/Domain/Presentation)
- ✅ Hilt Dependency Injection
- ✅ Room Database (7 entities, DAOs)
- ✅ Retrofit + OkHttp API client
- ✅ ViewModels для всех экранов
- ✅ Repository pattern
- ✅ Navigation с Bottom Bar
- ✅ Трекинг воды
- ✅ Трекинг веса
- ✅ Ежедневные задачи (ToDo)
- ✅ Система целей
- ✅ Избранные блюда
- ✅ Полная совместимость с Samsung S25 Ultra (Android 15)

## 📚 Полезные команды

```bash
# Backend логи
docker-compose logs -f backend

# Остановить все сервисы
docker-compose down

# Пересобрать и запустить
docker-compose up -d --build

# Сборка Android Debug APK
cd android && ./gradlew assembleDebug

# Очистка Android проекта
cd android && ./gradlew clean
```

## 🆘 Проблемы и решения

### Backend не запускается
```bash
# Проверьте логи
docker-compose logs backend

# Пересоздайте контейнеры
docker-compose down
docker-compose up -d --build
```

### Android Gradle ошибки
```bash
# Очистите кэш Gradle
cd android
./gradlew clean
./gradlew --stop

# В Android Studio: File -> Invalidate Caches and Restart
```

### База данных не работает
```bash
# Пересоздайте БД
docker-compose down -v
docker-compose up -d
```

## 📖 Дополнительно

Полная документация в [README.md](README.md)
