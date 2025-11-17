# CheckEat - Тестирование на macOS с Android Studio

Полная пошаговая инструкция для тестирования CheckEat Android приложения на macOS с использованием Android Studio.

## 📋 Требования

- **macOS**: 12 (Monterey) или новее
- **Процессор**: Intel или Apple Silicon (M1/M2/M3)
- **RAM**: Минимум 8GB (рекомендуется 16GB)
- **Свободное место**: 10GB+

---

## 🛠️ Установка необходимого ПО

### 1. Установка Homebrew

```bash
# Если еще не установлен
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Проверьте установку
brew --version
```

### 2. Установка Docker Desktop

```bash
# Установка через Homebrew
brew install --cask docker

# Или скачайте с https://www.docker.com/products/docker-desktop/

# Запустите Docker Desktop из Applications
open -a Docker

# Дождитесь запуска Docker и проверьте
docker --version
docker compose version
```

### 3. Установка JDK 17

```bash
# Установка через Homebrew
brew install openjdk@17

# Добавьте в PATH (добавьте в ~/.zshrc или ~/.bash_profile)
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc

# Перезагрузите терминал или выполните
source ~/.zshrc

# Проверьте установку
java -version
# Должно показать: openjdk version "17.x.x"
```

### 4. Установка Android Studio

**Вариант A - Через Homebrew:**
```bash
brew install --cask android-studio
```

**Вариант B - Скачать вручную:**
1. Откройте https://developer.android.com/studio
2. Скачайте Android Studio для macOS (выберите Apple Silicon или Intel)
3. Откройте .dmg файл и перетащите Android Studio в Applications
4. Запустите Android Studio

**Первый запуск Android Studio:**
1. Выберите `Don't import settings` (если это первая установка)
2. Выберите `Standard` installation
3. Выберите тему (Light/Dark)
4. Дождитесь загрузки SDK компонентов (это займёт 10-20 минут)

### 5. Настройка Android SDK

В Android Studio:
1. Откройте `Preferences` (⌘,) → `Appearance & Behavior` → `System Settings` → `Android SDK`
2. Во вкладке `SDK Platforms` установите:
   - ✅ Android 15.0 ("VanillaIceCream") API 35
   - ✅ Android 14.0 ("UpsideDownCake") API 34
   - ✅ Android 13.0 ("Tiramisu") API 33
3. Во вкладке `SDK Tools` установите:
   - ✅ Android SDK Build-Tools
   - ✅ Android Emulator
   - ✅ Android SDK Platform-Tools
   - ✅ Intel x86 Emulator Accelerator (если Intel процессор)
4. Нажмите `Apply` и дождитесь загрузки

### 6. Установка adb (Android Debug Bridge)

```bash
# Добавьте Android SDK в PATH (добавьте в ~/.zshrc)
echo 'export ANDROID_HOME=$HOME/Library/Android/sdk' >> ~/.zshrc
echo 'export PATH=$PATH:$ANDROID_HOME/emulator' >> ~/.zshrc
echo 'export PATH=$PATH:$ANDROID_HOME/platform-tools' >> ~/.zshrc

# Перезагрузите терминал
source ~/.zshrc

# Проверьте
adb version
```

---

## 🚀 Запуск Backend

### 1. Клонируйте репозиторий

```bash
cd ~/Projects  # или любая ваша рабочая директория
git clone <repository-url>
cd checkeat
```

### 2. Настройте переменные окружения

```bash
# Скопируйте пример конфигурации
cp .env.example .env

# Откройте в редакторе
nano .env
# или
code .env  # если установлен VS Code
```

**Добавьте ваш OpenAI API ключ:**
```env
# Получите ключ на https://platform.openai.com/api-keys
OPENAI_API_KEY=sk-your-actual-openai-api-key-here
OPENAI_MODEL=gpt-4-vision-preview
```

**Важно:** Без OpenAI API ключа анализ фото работать не будет!

### 3. Запустите Docker контейнеры

```bash
# Убедитесь что Docker Desktop запущен
docker ps

# Запустите backend
docker compose up -d

# Проверьте что все контейнеры запущены
docker compose ps
# Должны быть running: postgres, redis, backend

# Проверьте логи
docker compose logs -f backend
```

### 4. Проверьте работу backend

```bash
# Проверка health endpoint
curl http://localhost:8000/health
# Должно вернуть: {"status":"healthy"}

# Проверка API документации
open http://localhost:8000/docs
```

**Если возникли проблемы:**
```bash
# Остановите контейнеры
docker compose down

# Очистите
docker system prune -f

# Запустите заново
docker compose up -d
```

---

## 📱 Настройка Android проекта

### 1. Узнайте IP адрес вашего Mac

```bash
# Для Wi-Fi
ipconfig getifaddr en0

# Для Ethernet
ipconfig getifaddr en1

# Или используйте
ifconfig | grep "inet " | grep -v 127.0.0.1

# Запомните IP (например: 192.168.1.100)
```

### 2. Откройте проект в Android Studio

```bash
# Запустите Android Studio
open -a "Android Studio"

# Или через терминал откройте проект
open -a "Android Studio" ~/Projects/checkeat/android
```

В Android Studio:
1. Выберите `File` → `Open`
2. Найдите и выберите папку `checkeat/android`
3. Нажмите `Open`
4. Дождитесь синхронизации Gradle (5-10 минут при первом запуске)

### 3. Настройте API URL для реального устройства

Откройте `android/app/build.gradle.kts` и найдите секцию `productFlavors`:

```kotlin
productFlavors {
    create("dev") {
        dimension = "environment"
        applicationIdSuffix = ".dev"
        versionNameSuffix = "-dev"
        // Замените на IP вашего Mac!
        buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.100:8000/api/v1/\"")
    }
}
```

**Важно:** Замените `192.168.1.100` на реальный IP адрес вашего Mac!

### 4. Синхронизируйте Gradle

В Android Studio:
1. Нажмите `File` → `Sync Project with Gradle Files`
2. Или нажмите кнопку "Sync Now" если появится уведомление
3. Дождитесь завершения синхронизации

**Если возникли ошибки Gradle:**
```bash
cd ~/Projects/checkeat/android

# Очистите кэш
./gradlew clean

# Удалите .gradle директории
rm -rf .gradle build app/build

# Откройте снова в Android Studio
```

---

## 🖥️ Создание эмулятора Android

### 1. Откройте Device Manager

В Android Studio:
1. Нажмите на иконку телефона в правом верхнем углу
2. Или выберите `Tools` → `Device Manager`

### 2. Создайте новое виртуальное устройство

1. Нажмите `Create Device`
2. Выберите устройство:
   - **Для Apple Silicon (M1/M2/M3)**: Pixel 7 или Pixel 8
   - **Для Intel**: Pixel 5 или Pixel 6
3. Нажмите `Next`

### 3. Выберите System Image

1. Выберите вкладку `Recommended`
2. Выберите:
   - **Для Apple Silicon**: Android 14.0 (API 34) - Arm64
   - **Для Intel**: Android 14.0 (API 34) - x86_64
3. Нажмите `Download` если еще не скачано
4. Нажмите `Next`

### 4. Настройте AVD

1. Имя: `Pixel_7_API_34` (или любое другое)
2. В `Advanced Settings`:
   - RAM: 4096 MB (или больше если есть)
   - VM Heap: 512 MB
   - Internal Storage: 4096 MB
3. Нажмите `Finish`

### 5. Запустите эмулятор

1. В Device Manager найдите созданный эмулятор
2. Нажмите кнопку ▶️ `Play`
3. Дождитесь загрузки Android (первый запуск может занять 2-3 минуты)

**Примечание:** API URL для эмулятора уже настроен на `http://10.0.2.2:8000` (это localhost эмулятора)

---

## 🎯 Запуск приложения

### На эмуляторе:

1. Убедитесь что эмулятор запущен
2. В Android Studio выберите эмулятор в списке устройств (вверху)
3. Нажмите зелёную кнопку `Run` ▶️ (или ⌃R)
4. Дождитесь сборки и установки APK
5. Приложение откроется автоматически

### На реальном устройстве (iPhone кабелем):

**1. Включите режим разработчика на Android устройстве:**
- Откройте `Настройки` → `О телефоне` → `Сведения о ПО`
- Нажмите 7 раз на `Номер сборки`
- Появится сообщение "Вы стали разработчиком!"

**2. Включите отладку по USB:**
- Откройте `Настройки` → `Параметры разработчика`
- Включите `Отладка по USB`

**3. Подключите устройство к Mac:**
- Используйте USB кабель
- На телефоне разрешите отладку
- Выберите `Всегда разрешать с этого компьютера`

**4. Проверьте подключение:**
```bash
adb devices
# Должно показать ваше устройство
```

**5. Запустите приложение:**
- В Android Studio выберите ваше устройство
- Нажмите `Run` ▶️
- На телефоне разрешите установку из неизвестных источников

---

## ✅ Тестирование функционала

### 1. Регистрация и вход

1. Откройте приложение
2. Нажмите `Зарегистрироваться`
3. Введите email и пароль
4. Проверьте что вход выполнен успешно

### 2. Анализ фото еды

1. Перейдите на вкладку `Камера`
2. Разрешите доступ к камере
3. Сделайте фото еды или выберите из галереи
4. Дождитесь анализа (10-30 секунд)
5. Проверьте результаты:
   - Калории
   - Белки, жиры, углеводы
   - Ингредиенты
   - Советы по здоровью и вкусу

### 3. История анализов

1. Перейдите на вкладку `История`
2. Проверьте что ваш анализ сохранился
3. Попробуйте удалить анализ

### 4. Статистика

1. Перейдите на вкладку `Статистика`
2. Проверьте дневную статистику
3. Посмотрите общую сумму калорий

### 5. Заметки

1. Перейдите на вкладку `Заметки`
2. Создайте новую заметку
3. Добавьте теги
4. Проверьте поиск

---

## 🐛 Troubleshooting

### Приложение не подключается к backend

**Проблема:** `Network error` или `Unable to connect`

**Решение для эмулятора:**
```bash
# Убедитесь что backend запущен
curl http://localhost:8000/health

# Должно вернуть {"status":"healthy"}
```

**Решение для реального устройства:**
```bash
# 1. Проверьте IP адрес Mac
ifconfig | grep "inet " | grep -v 127.0.0.1

# 2. Убедитесь что телефон и Mac в одной Wi-Fi сети

# 3. Проверьте firewall на Mac
# System Settings → Network → Firewall
# Разрешите Docker Desktop

# 4. Проверьте доступность с телефона
# Откройте браузер на телефоне:
# http://YOUR_MAC_IP:8000/health

# 5. Если не работает - используйте adb reverse
adb reverse tcp:8000 tcp:8000
# Теперь можно использовать http://localhost:8000 на устройстве
```

---

### Gradle sync failed

**Проблема:** Ошибки синхронизации Gradle

**Решение:**
```bash
cd ~/Projects/checkeat/android

# Очистите проект
./gradlew clean

# Удалите кэш
rm -rf .gradle
rm -rf build
rm -rf app/build

# В Android Studio:
# File → Invalidate Caches → Invalidate and Restart
```

---

### Эмулятор не запускается

**Проблема:** Эмулятор зависает или не запускается

**Решение для Apple Silicon:**
```bash
# Убедитесь что выбран ARM64 образ, а не x86_64

# Проверьте виртуализацию
sysctl kern.hv_support
# Должно вернуть: kern.hv_support: 1
```

**Решение для Intel:**
```bash
# Установите HAXM (Intel Hardware Accelerated Execution Manager)
brew install --cask intel-haxm

# Или скачайте с:
# https://github.com/intel/haxm/releases
```

---

### OpenAI API ошибки

**Проблема:** `Invalid API key` или `Rate limit exceeded`

**Решение:**
```bash
# 1. Проверьте API ключ в .env
cat .env | grep OPENAI_API_KEY

# 2. Проверьте баланс на https://platform.openai.com/account/usage

# 3. Проверьте логи backend
docker compose logs -f backend

# 4. Убедитесь что используете правильную модель
# gpt-4-vision-preview - для GPT-4 Vision
# gpt-4o - для GPT-4 Omni (новейшая)
```

---

## 🔧 Полезные команды для macOS

### Docker

```bash
# Проверка статуса
docker compose ps

# Логи backend
docker compose logs -f backend

# Перезапуск
docker compose restart backend

# Остановка всех контейнеров
docker compose down

# Полная очистка
docker compose down -v
docker system prune -a
```

### Android

```bash
# Список устройств
adb devices

# Логи приложения
adb logcat | grep CheckEat

# Установка APK
adb install app/build/outputs/apk/dev/debug/app-dev-debug.apk

# Удаление приложения
adb uninstall com.checkeat.dev

# Проброс портов
adb reverse tcp:8000 tcp:8000

# Скриншот
adb exec-out screencap -p > screenshot.png
```

### Gradle

```bash
cd ~/Projects/checkeat/android

# Очистка
./gradlew clean

# Сборка Debug
./gradlew assembleDebug

# Сборка Dev Debug
./gradlew assembleDevDebug

# Запуск тестов
./gradlew test

# Список задач
./gradlew tasks
```

---

## 📊 Мониторинг производительности

### Android Profiler

В Android Studio:
1. Запустите приложение
2. Откройте `View` → `Tool Windows` → `Profiler`
3. Выберите ваш процесс
4. Мониторьте:
   - CPU usage
   - Memory usage
   - Network activity

### Logcat

В Android Studio:
1. Откройте `View` → `Tool Windows` → `Logcat`
2. Фильтр по `com.checkeat`
3. Смотрите логи в реальном времени

---

## 🎓 Дополнительные ресурсы

- **Android Developer**: https://developer.android.com
- **Jetpack Compose**: https://developer.android.com/jetpack/compose
- **OpenAI API Docs**: https://platform.openai.com/docs
- **Docker Desktop for Mac**: https://docs.docker.com/desktop/install/mac-install/

---

## ✅ Checklist перед тестированием

- [ ] Docker Desktop запущен
- [ ] Backend доступен на http://localhost:8000
- [ ] OpenAI API ключ добавлен в .env
- [ ] JDK 17 установлен
- [ ] Android Studio настроен
- [ ] Эмулятор создан и работает
- [ ] Gradle sync успешно выполнен
- [ ] Для реального устройства: IP настроен в build.gradle.kts
- [ ] Для реального устройства: телефон и Mac в одной Wi-Fi

---

**Готово!** Теперь вы можете полноценно тестировать CheckEat на macOS! 🎉

Если возникли проблемы - см. секцию Troubleshooting выше.
