"""
Утилиты для работы с файлами
"""
import os
import uuid
from datetime import datetime
from PIL import Image
from fastapi import UploadFile, HTTPException
from ..core.config import settings


def validate_image_file(file: UploadFile) -> None:
    """
    Валидация загружаемого файла изображения

    Args:
        file: Загружаемый файл

    Raises:
        HTTPException: Если файл не валиден
    """
    # Проверка расширения
    file_ext = file.filename.split(".")[-1].lower()
    if file_ext not in settings.ALLOWED_EXTENSIONS:
        raise HTTPException(
            status_code=400,
            detail=f"Недопустимый формат файла. Разрешены: {', '.join(settings.ALLOWED_EXTENSIONS)}"
        )


def save_upload_file(file: UploadFile, user_id: int) -> str:
    """
    Сохранение загруженного файла

    Args:
        file: Загружаемый файл
        user_id: ID пользователя

    Returns:
        str: Путь к сохранённому файлу
    """
    # Валидация
    validate_image_file(file)

    # Генерация уникального имени файла
    file_ext = file.filename.split(".")[-1].lower()
    unique_filename = f"{user_id}_{datetime.now().strftime('%Y%m%d_%H%M%S')}_{uuid.uuid4().hex[:8]}.{file_ext}"

    # Создание директории для пользователя
    user_dir = os.path.join(settings.UPLOAD_DIR, str(user_id))
    os.makedirs(user_dir, exist_ok=True)

    # Путь к файлу
    file_path = os.path.join(user_dir, unique_filename)

    # Сохранение файла
    with open(file_path, "wb") as buffer:
        buffer.write(file.file.read())

    # Оптимизация изображения
    optimize_image(file_path)

    return file_path


def optimize_image(file_path: str, max_width: int = 1920, max_height: int = 1920, quality: int = 85) -> None:
    """
    Оптимизация изображения (сжатие и изменение размера)

    Args:
        file_path: Путь к изображению
        max_width: Максимальная ширина
        max_height: Максимальная высота
        quality: Качество сжатия (1-100)
    """
    try:
        with Image.open(file_path) as img:
            # Конвертация в RGB если нужно
            if img.mode in ("RGBA", "P"):
                img = img.convert("RGB")

            # Изменение размера если нужно
            if img.width > max_width or img.height > max_height:
                img.thumbnail((max_width, max_height), Image.Resampling.LANCZOS)

            # Сохранение с оптимизацией
            img.save(file_path, optimize=True, quality=quality)

    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Ошибка при обработке изображения: {str(e)}")


def delete_file(file_path: str) -> None:
    """
    Удаление файла

    Args:
        file_path: Путь к файлу
    """
    if os.path.exists(file_path):
        os.remove(file_path)


def get_file_url(file_path: str) -> str:
    """
    Получение URL файла

    Args:
        file_path: Путь к файлу

    Returns:
        str: URL файла
    """
    # Если используется S3, вернуть S3 URL
    # Пока возвращаем относительный путь
    return file_path.replace(settings.UPLOAD_DIR, "/uploads")
