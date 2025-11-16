# Быстрый старт CheckEat

## 🚀 Запуск за 5 минут

### 1. Запуск Backend

```bash
# Создайте .env файл
cd backend
cp .env.example .env

# Добавьте ваш Anthropic API ключ в .env
# ANTHROPIC_API_KEY=sk-ant-xxx...

# Запустите через Docker
cd ..
docker-compose up -d

# Проверьте что всё работает
curl http://localhost:8000/health
# Должно вернуть: {"status":"healthy"}
```

**API документация**: http://localhost:8000/docs

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

```bash
# Откройте Android Studio
# File -> Open -> выберите папку android/

# Дождитесь синхронизации Gradle

# Нажмите Run (зелёная кнопка)
# Выберите эмулятор или устройство
```

## 📝 Что реализовано

### Backend (100% готов)
- ✅ Регистрация и вход
- ✅ Анализ фото через Claude Vision
- ✅ История анализов
- ✅ Система заметок (CRUD)
- ✅ Статистика (день/неделя/месяц)
- ✅ Docker контейнеризация

### Android (Базовая структура)
- ✅ Gradle конфигурация
- ✅ Clean Architecture
- ✅ Hilt DI
- ✅ Навигация
- ⚠️ UI экраны (заглушки - нужно реализовать)

## 🔧 Что нужно доработать в Android

1. **Room Database** - создать entities и DAOs
2. **Retrofit API** - создать API interface
3. **Repository** - реализовать data layer
4. **ViewModels** - создать для каждого экрана
5. **UI Screens** - реализовать полноценные экраны:
   - CameraScreen (CameraX + загрузка фото)
   - HistoryScreen (список анализов)
   - StatsScreen (графики)
   - NotesScreen (CRUD заметок)
   - ProfileScreen (настройки)

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
