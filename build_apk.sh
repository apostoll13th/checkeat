#!/bin/bash

# Скрипт для сборки APK

echo "🚀 CheckEat - Сборка APK"
echo "========================"

# Проверка наличия Android проекта
if [ ! -d "android" ]; then
    echo "❌ Директория android не найдена!"
    exit 1
fi

cd android

echo "📦 Очистка предыдущей сборки..."
./gradlew clean

echo "🔨 Сборка Debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo "✅ Debug APK собран успешно!"
    echo "📍 Путь: android/app/build/outputs/apk/debug/app-debug.apk"
else
    echo "❌ Ошибка при сборке APK"
    exit 1
fi

echo ""
echo "Для сборки Release APK:"
echo "1. Создайте keystore: keytool -genkey -v -keystore checkeat.keystore -alias checkeat -keyalg RSA -keysize 2048 -validity 10000"
echo "2. Настройте signingConfig в app/build.gradle.kts"
echo "3. Запустите: ./gradlew assembleRelease"
