# CheckEat - Установка на Samsung S25 Ultra

Пошаговая инструкция по установке и запуску CheckEat на Samsung S25 Ultra.

## 📱 Требования

- **Samsung S25 Ultra** с Android 15 (API 35)
- **Компьютер** с запущенным backend (в одной WiFi сети)
- **USB кабель** для подключения телефона
- **ADB драйверы** (обычно устанавливаются автоматически)

---

## 🚀 Быстрая установка

### Шаг 1: Подготовка телефона

1. **Включите режим разработчика:**
   - Откройте: `Настройки` → `О телефоне` → `Сведения о ПО`
   - Нажмите 7 раз на `Номер сборки`
   - Появится сообщение "Вы стали разработчиком!"

2. **Включите отладку по USB:**
   - Откройте: `Настройки` → `Параметры разработчика`
   - Включите: `Отладка по USB`
   - Включите: `Установка через USB` (опционально)

3. **Подключите телефон к компьютеру:**
   - Используйте USB кабель
   - На телефоне разрешите отладку по USB
   - Выберите `Всегда разрешать с этого компьютера`

4. **Проверьте подключение:**
   ```bash
   adb devices
   # Должен показать ваш S25 Ultra
   ```

---

### Шаг 2: Запуск backend

1. **Узнайте IP адрес вашего компьютера:**

   **Windows:**
   ```cmd
   ipconfig
   # Найдите "IPv4 Address" для вашей WiFi сети
   # Например: 192.168.1.100
   ```

   **Mac/Linux:**
   ```bash
   ifconfig | grep inet
   # Или
   ip addr show
   # Найдите IP для вашей WiFi (обычно 192.168.x.x)
   ```

2. **Запустите backend:**
   ```bash
   cd checkeat
   docker compose up -d

   # Проверьте что backend работает
   curl http://localhost:8000/health
   ```

3. **Проверьте доступность с телефона:**
   - Убедитесь что телефон и компьютер в одной WiFi сети
   - Backend должен быть доступен по IP: `http://YOUR_IP:8000`

---

### Шаг 3: Настройка API URL

#### Вариант A: Сборка Dev версии (рекомендуется)

1. **Отредактируйте `android/app/build.gradle.kts`:**
   ```kotlin
   productFlavors {
       create("dev") {
           dimension = "environment"
           applicationIdSuffix = ".dev"
           versionNameSuffix = "-dev"
           // ЗАМЕНИТЕ на IP вашего компьютера!
           buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.100:8000/api/v1/\"")
       }
   }
   ```

2. **Соберите Dev APK:**
   ```bash
   # Через Docker
   make android-build

   # Или напрямую
   cd android
   ./gradlew assembleDevDebug
   ```

#### Вариант B: Редактирование Release конфига

1. **Отредактируйте `android/app/build.gradle.kts`:**
   ```kotlin
   buildTypes {
       release {
           // ЗАМЕНИТЕ YOUR_COMPUTER_IP на ваш реальный IP!
           buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.100:8000/api/v1/\"")
       }
   }
   ```

2. **Соберите Release APK:**
   ```bash
   make android-release
   ```

---

### Шаг 4: Установка APK

#### Через ADB (рекомендуется):

```bash
# Dev версия
adb install android/app/build/outputs/apk/dev/debug/app-dev-debug.apk

# Или Release версия
adb install android/app/build/outputs/apk/prod/release/app-prod-release.apk
```

#### Через файловый менеджер:

1. Скопируйте APK на телефон
2. Откройте файл через `Мои файлы`
3. Разрешите установку из неизвестных источников
4. Установите приложение

---

### Шаг 5: Первый запуск

1. **Запустите CheckEat** на Samsung S25 Ultra

2. **Разрешите необходимые права:**
   - ✅ Камера - для фото еды
   - ✅ Хранилище - для сохранения фото
   - ✅ Уведомления - для напоминаний (опционально)

3. **Проверьте подключение:**
   - Приложение должно подключиться к backend
   - Попробуйте сфотографировать еду
   - Проверьте что анализ работает

---

## 🔧 Настройка Network Security

Приложение уже настроено для работы с HTTP в локальной сети.

**Конфигурация** (`android/app/src/main/res/xml/network_security_config.xml`):
```xml
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="true">192.168.1.100</domain>
    <!-- Добавьте ваш IP адрес -->
</domain-config>
```

Если у вас другой IP адрес:
1. Отредактируйте `network_security_config.xml`
2. Добавьте ваш IP диапазон (например, `192.168.0.0`)
3. Пересоберите APK

---

## 🐛 Troubleshooting

### Приложение не подключается к backend

**Проблема:** "Unable to connect" или "Network error"

**Решение:**
1. Проверьте что backend запущен: `curl http://localhost:8000/health`
2. Проверьте что телефон и компьютер в одной WiFi сети
3. Проверьте firewall на компьютере (разрешите порт 8000)
4. Проверьте IP адрес в build.gradle.kts
5. Проверьте логи:
   ```bash
   adb logcat | grep CheckEat
   ```

**Windows Firewall:**
```powershell
# Разрешить порт 8000
netsh advfirewall firewall add rule name="CheckEat Backend" dir=in action=allow protocol=TCP localport=8000
```

**Mac Firewall:**
```bash
# Откройте Системные настройки → Безопасность → Firewall
# Разрешите входящие подключения для Docker
```

---

### Камера не работает

**Проблема:** Ошибка при открытии камеры

**Решение:**
1. Проверьте что разрешение на камеру дано
2. Перезапустите приложение
3. Проверьте логи: `adb logcat | grep Camera`
4. Переустановите приложение

---

### APK не устанавливается

**Проблема:** "App not installed" или ошибка установки

**Решение:**
1. Удалите старую версию приложения
2. Включите `Установка из неизвестных источников`
3. Проверьте что APK не поврежден (пересоберите)
4. Проверьте место на диске

---

### Backend недоступен с телефона

**Решение 1 - Проверка сети:**
```bash
# На телефоне откройте браузер и перейдите:
http://YOUR_IP:8000/health

# Должен показать: {"status": "healthy"}
```

**Решение 2 - Проверка firewall:**
```bash
# Linux
sudo ufw allow 8000

# Mac
# System Preferences → Security & Privacy → Firewall → Firewall Options
# Разрешите Docker Desktop
```

**Решение 3 - Использование ADB reverse:**
```bash
# Проброс порта с компьютера на телефон
adb reverse tcp:8000 tcp:8000

# Теперь можно использовать http://localhost:8000 на телефоне
```

---

## 📊 Оптимизация для S25 Ultra

### Камера высокого разрешения

S25 Ultra имеет камеру 200MP. Для оптимальной работы:

1. **Автоматическое сжатие** фото перед отправкой (уже реализовано)
2. **Качество**: 80% JPEG compression
3. **Максимальный размер**: 2048x2048px

### Производительность

S25 Ultra оптимизирован для:
- ✅ Jetpack Compose UI
- ✅ Room Database с Flow
- ✅ Coroutines для асинхронности
- ✅ Coil для эффективной загрузки изображений

### Батарея

Приложение оптимизировано для экономии батареи:
- Кэширование запросов через Redis
- Offline-first архитектура
- Background sync только при необходимости

---

## 🔐 Безопасность

### Разрешения

Приложение запрашивает только необходимые разрешения:
- ✅ `CAMERA` - для фото еды
- ✅ `READ_MEDIA_IMAGES` - для доступа к галерее (Android 13+)
- ✅ `INTERNET` - для связи с backend
- ✅ `POST_NOTIFICATIONS` - для уведомлений (опционально)

### Network Security

- HTTP разрешен только для локальной разработки
- В production используется HTTPS
- Network Security Config ограничивает cleartext домены

---

## 📱 Build Variants

Приложение поддерживает несколько вариантов сборки:

### Dev (Development)
- **ID**: `com.checkeat.dev`
- **API**: Локальный backend
- **Логирование**: Включено
- **Использование**: Разработка на реальном устройстве

```bash
./gradlew assembleDevDebug
adb install app/build/outputs/apk/dev/debug/app-dev-debug.apk
```

### Prod (Production)
- **ID**: `com.checkeat`
- **API**: Production backend
- **Логирование**: Минимальное
- **Оптимизация**: ProGuard + R8
- **Использование**: Релиз в Google Play

```bash
./gradlew assembleProdRelease
```

---

## 🎯 Быстрые команды

### Полный цикл разработки:

```bash
# 1. Запустить backend
cd checkeat
docker compose up -d

# 2. Узнать IP компьютера
ifconfig | grep inet  # Mac/Linux
ipconfig              # Windows

# 3. Обновить API URL в android/app/build.gradle.kts
# buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.100:8000/api/v1/\"")

# 4. Собрать и установить APK
cd android
./gradlew assembleDevDebug && adb install -r app/build/outputs/apk/dev/debug/app-dev-debug.apk

# 5. Запустить приложение
adb shell am start -n com.checkeat.dev/.presentation.MainActivity

# 6. Смотреть логи
adb logcat | grep CheckEat
```

---

## 📞 Помощь

### Логи

**Backend логи:**
```bash
docker compose logs -f backend
```

**Android логи:**
```bash
# Все логи CheckEat
adb logcat | grep CheckEat

# Только ошибки
adb logcat *:E | grep CheckEat

# Network запросы
adb logcat | grep OkHttp
```

### Отладка Network

**Проверка подключения:**
```bash
# С компьютера
curl http://localhost:8000/health

# С телефона (через adb shell)
adb shell
curl http://YOUR_IP:8000/health
```

**Мониторинг сети:**
```bash
# Мониторинг HTTP запросов
adb logcat | grep "OkHttp\|Retrofit"
```

---

## ✅ Checklist перед использованием

- [ ] Backend запущен и доступен
- [ ] Телефон и компьютер в одной WiFi
- [ ] IP адрес компьютера узнан
- [ ] API URL обновлен в build.gradle.kts
- [ ] APK собран и установлен
- [ ] Режим разработчика включен
- [ ] Отладка по USB включена
- [ ] Все разрешения даны приложению
- [ ] Firewall разрешает порт 8000

---

**Готово!** Теперь CheckEat должен работать на вашем Samsung S25 Ultra! 🎉

Если возникли проблемы - смотрите секцию Troubleshooting выше или проверьте логи с помощью `adb logcat`.
