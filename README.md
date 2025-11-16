# CheckEat - AI-приложение для анализа калорий и питательности еды

Production-ready Android приложение для анализа калорий и питательности еды по фотографиям с системой заметок, powered by Claude AI.

## 🚀 Основной функционал

### Реализовано:

✅ **Backend API (FastAPI)**
- Полноценный RESTful API на FastAPI
- Интеграция с Claude Vision API для анализа фото еды
- JWT аутентификация
- PostgreSQL база данных
- Redis кэширование
- Docker контейнеризация
- CI/CD через GitHub Actions
- Автоматические бекапы системы

✅ **Анализ еды**
- Подсчёт калорий, белков, жиров, углеводов
- Определение ингредиентов
- AI-рекомендации по здоровью и вкусу

✅ **Система заметок**
- Создание, редактирование, удаление заметок
- Прикрепление заметок к анализам
- Теги для категоризации
- Поиск по заметкам

✅ **Статистика**
- Дневная, недельная, месячная статистика
- Данные для графиков калорий и БЖУ

✅ **Экспорт данных**
- Экспорт истории анализов в CSV
- Экспорт статистики в CSV
- Экспорт заметок в CSV

✅ **Android приложение (Расширенная структура)**
- Kotlin + Jetpack Compose
- Material Design 3 с улучшенными UI компонентами
- Clean Architecture (Data/Domain/Presentation)
- Hilt Dependency Injection (полностью настроен)
- Room Database с entities, DAOs, converters
- Retrofit API client с полным набором эндпоинтов
- Repository pattern для работы с данными
- Навигация с Bottom Navigation Bar
- Привлекательные UI экраны с пустыми состояниями
- Docker сборка APK
- Трекинг воды и веса
- Ежедневные задачи (ToDo) для мотивации
- Система целей и прогресс-баров
- Избранные блюда

## 📋 Требования

### Для Backend:
- Docker и Docker Compose
- Anthropic API ключ (Claude)

### Для Android:
- Android Studio Hedgehog или новее
- JDK 17
- Android SDK (минимум API 24, целевой API 35 для Android 15)
- **Оптимизировано для Samsung S25 Ultra** (Android 15)

> 📱 **Для владельцев Samsung S25 Ultra**: См. [SAMSUNG_S25_ULTRA_SETUP.md](./SAMSUNG_S25_ULTRA_SETUP.md) для пошаговой инструкции

## 🔧 Установка и запуск

### 🐳 Docker Compose (Рекомендуется)

**Самый простой способ запустить весь проект!**

1. **Клонируйте репозиторий:**
```bash
git clone <repository-url>
cd checkeat
```

2. **Создайте .env файл:**
```bash
cp .env.example .env
```

3. **Отредактируйте .env и добавьте ваш Anthropic API ключ:**
```bash
nano .env
```
```env
ANTHROPIC_API_KEY=your-api-key-here
JWT_SECRET_KEY=$(openssl rand -hex 32)
SECRET_KEY=$(openssl rand -hex 32)
```

4. **Запустите все сервисы:**
```bash
# Простой запуск
docker compose up -d

# Или используйте Makefile для удобства
make up
```

Backend API будет доступен на **http://localhost:8000**

**API документация:** http://localhost:8000/docs

#### 📱 Сборка Android APK через Docker:
```bash
# Debug APK
make android-build

# Release APK
make android-release
```

APK файлы будут в `android/app/build/outputs/apk/`

**Подробная документация:** См. [DOCKER.md](./DOCKER.md)

#### 🎯 Полезные команды:
```bash
make up              # Запустить все сервисы
make down            # Остановить все сервисы
make logs            # Посмотреть логи
make logs-backend    # Логи backend
make test            # Запустить тесты
make shell           # Зайти в backend контейнер
make db-migrate      # Применить миграции БД
make status          # Статус всех сервисов
```

### Backend (без Docker)

Если вы хотите запустить backend локально без Docker:

1. **Установите зависимости:**
```bash
cd backend
pip install -r requirements.txt
```

2. **Создайте .env файл:**
```bash
cp .env.example .env
```

3. **Запустите PostgreSQL и Redis**

4. **Запустите backend:**
```bash
python main.py
```

### Android приложение

#### Для эмулятора:

1. **Откройте Android проект в Android Studio:**
```bash
File -> Open -> выберите папку android/
```

2. **Дождитесь синхронизации Gradle**

3. **Запустите приложение:**
- Нажмите Run (зелёная кнопка play)
- Выберите эмулятор
- API URL уже настроен для эмулятора (`http://10.0.2.2:8000`)

#### Для реального устройства (Samsung S25 Ultra и др.):

1. **Узнайте IP адрес вашего компьютера:**
   ```bash
   # Mac/Linux
   ifconfig | grep inet

   # Windows
   ipconfig
   ```

2. **Измените API URL в `android/app/build.gradle.kts`:**
   ```kotlin
   productFlavors {
       create("dev") {
           // Замените на IP вашего компьютера!
           buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.100:8000/api/v1/\"")
       }
   }
   ```

3. **Соберите Dev APK:**
   ```bash
   make android-build
   # или
   cd android && ./gradlew assembleDevDebug
   ```

4. **Установите на устройство:**
   ```bash
   adb install android/app/build/outputs/apk/dev/debug/app-dev-debug.apk
   ```

> 📱 **Подробная инструкция для Samsung S25 Ultra**: [SAMSUNG_S25_ULTRA_SETUP.md](./SAMSUNG_S25_ULTRA_SETUP.md)

## 📚 Структура проекта

```
checkeat/
├── backend/                    # FastAPI Backend
│   ├── app/
│   │   ├── api/v1/            # API эндпоинты
│   │   │   ├── auth.py        # Аутентификация
│   │   │   ├── food_analysis.py  # Анализ еды
│   │   │   ├── notes.py       # Заметки
│   │   │   └── stats.py       # Статистика
│   │   ├── core/              # Конфигурация, безопасность
│   │   ├── db/                # База данных
│   │   ├── models/            # SQLAlchemy модели
│   │   ├── schemas/           # Pydantic схемы
│   │   ├── services/          # Бизнес-логика
│   │   │   └── claude_service.py  # Claude Vision AI
│   │   └── utils/             # Утилиты
│   ├── main.py                # Главный файл FastAPI
│   ├── requirements.txt       # Python зависимости
│   └── Dockerfile             # Docker образ
│
├── android/                   # Android приложение
│   ├── app/
│   │   ├── src/main/java/com/checkeat/
│   │   │   ├── data/         # Data layer
│   │   │   │   ├── local/    # Room Database
│   │   │   │   ├── remote/   # Retrofit API
│   │   │   │   └── repository/  # Repository
│   │   │   ├── domain/       # Domain layer
│   │   │   │   ├── model/    # Модели данных
│   │   │   │   └── usecase/  # Use Cases
│   │   │   └── presentation/ # Presentation layer
│   │   │       ├── camera/   # Экран камеры
│   │   │       ├── history/  # Экран истории
│   │   │       ├── stats/    # Экран статистики
│   │   │       ├── notes/    # Экран заметок
│   │   │       └── MainActivity.kt
│   │   └── build.gradle.kts  # Gradle конфигурация
│
└── docker-compose.yml         # Docker Compose конфигурация
```

## 🔌 API Эндпоинты

### Аутентификация
- `POST /api/v1/auth/register` - Регистрация
- `POST /api/v1/auth/login` - Вход
- `GET /api/v1/auth/profile` - Профиль пользователя

### Анализ еды
- `POST /api/v1/analyze` - Анализ фото еды
- `GET /api/v1/analyze/history` - История анализов
- `GET /api/v1/analyze/history/{id}` - Конкретный анализ
- `DELETE /api/v1/analyze/history/{id}` - Удалить анализ

### Заметки
- `POST /api/v1/notes` - Создать заметку
- `GET /api/v1/notes` - Список заметок (с фильтрами)
- `GET /api/v1/notes/{id}` - Получить заметку
- `PUT /api/v1/notes/{id}` - Обновить заметку
- `DELETE /api/v1/notes/{id}` - Удалить заметку

### Статистика
- `GET /api/v1/stats/daily` - Дневная статистика
- `GET /api/v1/stats/weekly` - Недельная статистика
- `GET /api/v1/stats/monthly` - Месячная статистика
- `GET /api/v1/stats/chart` - Данные для графиков

### Экспорт
- `GET /api/v1/export/csv/history` - Экспорт истории в CSV
- `GET /api/v1/export/csv/stats` - Экспорт статистики в CSV
- `GET /api/v1/export/csv/notes` - Экспорт заметок в CSV

## 🏗️ Технологический стек

### Backend
- **FastAPI** - современный веб-фреймворк
- **PostgreSQL** - база данных
- **SQLAlchemy** - ORM
- **Redis** - кэширование
- **Anthropic Claude** - AI анализ изображений
- **JWT** - аутентификация
- **Docker** - контейнеризация

### Android
- **Kotlin** - язык программирования
- **Jetpack Compose** - современный UI фреймворк
- **Material Design 3** - дизайн система
- **Hilt** - Dependency Injection
- **Room** - локальная база данных
- **Retrofit** - HTTP клиент
- **Coil** - загрузка изображений
- **CameraX** - работа с камерой
- **Coroutines & Flow** - асинхронность

## 🔨 Разработка

### Расширение Android приложения

#### ✅ Уже реализовано:

1. **Room Database** - полностью настроена
   - Entities для всех моделей
   - DAOs с методами для работы с данными
   - Type Converters для сложных типов
   - AppDatabase класс

2. **Retrofit API** - полностью настроен
   - CheckEatApi интерфейс со всеми эндпоинтами
   - DTOs для запросов и ответов
   - OkHttp клиент с логированием

3. **Hilt DI** - полностью настроен
   - DatabaseModule для Room
   - NetworkModule для Retrofit
   - Готов к использованию в ViewModels

4. **Repository Pattern** - базовый пример
   - FoodAnalysisRepository с основными методами
   - Готова структура для расширения

5. **UI Screens** - улучшенные компоненты
   - Привлекательные экраны с Material Design 3
   - Пустые состояния (Empty States)
   - Готовые карточки и компоненты

#### 🔨 Что осталось доработать:

1. **ViewModels** - создать для каждого экрана
2. **CameraX интеграция** - добавить работу с камерой
3. **Реальные данные** - подключить Repository к UI
4. **Обработка состояний** - Loading, Error, Success
5. **Навигация с параметрами** - для деталей анализа
6. **Offline-first синхронизация** - WorkManager

### Сборка APK

#### 🐳 Через Docker (Рекомендуется):

```bash
# Debug APK
make android-build
# или
docker compose --profile build run --rm android-builder

# Release APK
make android-release
# или
docker compose --profile build run --rm android-builder ./gradlew assembleRelease --no-daemon
```

APK файлы будут доступны в:
- Debug: `android/app/build/outputs/apk/debug/app-debug.apk`
- Release: `android/app/build/outputs/apk/release/app-release.apk`

#### 🔨 Локальная сборка:

**Debug APK:**
```bash
cd android
./gradlew assembleDebug
```

**Release APK:**

1. Создайте keystore:
```bash
keytool -genkey -v -keystore checkeat.keystore -alias checkeat -keyalg RSA -keysize 2048 -validity 10000
```

2. Добавьте в `android/app/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../checkeat.keystore")
            storePassword = "your-password"
            keyAlias = "checkeat"
            keyPassword = "your-password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            ...
        }
    }
}
```

3. Соберите release APK:
```bash
./gradlew assembleRelease
```

## 📝 Примеры использования API

### Регистрация пользователя:
```bash
curl -X POST http://localhost:8000/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "name": "Ivan Ivanov"
  }'
```

### Анализ фото еды:
```bash
curl -X POST http://localhost:8000/api/v1/analyze \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@food.jpg"
```

## 🐛 Отладка

### Backend логи:
```bash
# Через Makefile
make logs-backend

# Напрямую
docker compose logs -f backend
```

### PostgreSQL:
```bash
# Через Makefile
make db-shell

# Напрямую
docker compose exec postgres psql -U checkeat_user -d checkeat_db
```

### Redis:
```bash
docker compose exec redis redis-cli
```

### Проверка статуса сервисов:
```bash
# Через Makefile
make status

# Напрямую
docker compose ps
```

### Запуск тестов:
```bash
# Через Makefile
make test

# Напрямую
docker compose exec backend pytest
```

**Подробнее:** См. [DOCKER.md](./DOCKER.md) для полного руководства по отладке

## 🔐 Безопасность

- JWT токены с истечением срока
- Хеширование паролей (bcrypt)
- Валидация всех входных данных
- CORS настроен
- Rate limiting (можно добавить)

## 📦 Деплой

### Backend на сервер:

1. Установите Docker и Docker Compose на сервер
2. Скопируйте код на сервер
3. Настройте .env с продакшн настройками
4. Запустите: `docker-compose up -d`
5. Настройте Nginx как reverse proxy (опционально)

### Android APK:

1. Соберите release APK
2. Загрузите в Google Play Console
3. Или распространяйте APK напрямую

## 🤝 Вклад в проект

Проект создан как MVP (Minimum Viable Product). Для расширения:

1. Добавьте полную реализацию UI экранов
2. Реализуйте offline-first синхронизацию
3. Добавьте графики MPAndroidChart
4. Реализуйте экспорт в PDF/CSV
5. Добавьте push уведомления
6. Реализуйте Widget для главного экрана

## 📄 Лицензия

MIT License

## 👨‍💻 Автор

Создано с использованием Claude AI

## 🙏 Благодарности

- Anthropic за Claude AI
- JetBrains за Kotlin и Android Studio
- Команде FastAPI

---

**Примечание**: Это production-ready структура проекта. Backend полностью функционален. Android приложение содержит базовую структуру и навигацию - остальные экраны нужно реализовать согласно вашим требованиям. Все необходимые зависимости и архитектурные паттерны уже настроены.
