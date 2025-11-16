# CheckEat Android Build Dockerfile
FROM openjdk:17-slim

# Установка необходимых инструментов
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    git \
    && rm -rf /var/lib/apt/lists/*

# Установка Android SDK
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV ANDROID_HOME=${ANDROID_SDK_ROOT}
ENV PATH=${PATH}:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools

# Скачивание Android command line tools
RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/cmdline-tools.zip && \
    unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools && \
    mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest && \
    rm /tmp/cmdline-tools.zip

# Принятие лицензий и установка необходимых SDK компонентов
RUN yes | sdkmanager --licenses && \
    sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# Рабочая директория
WORKDIR /app

# Копирование gradle wrapper
COPY gradle gradle
COPY gradlew .
COPY gradle.properties .

# Копирование gradle конфигурации
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Копирование исходников приложения
COPY app app

# Создание директории для вывода
RUN mkdir -p /app/build/outputs

# Скачивание зависимостей gradle
RUN ./gradlew dependencies --no-daemon || true

# Команда по умолчанию - сборка Debug APK
CMD ["./gradlew", "assembleDebug", "--no-daemon", "--stacktrace"]
