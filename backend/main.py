"""
CheckEat API - Главный файл приложения
"""
import logging
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from app.core.config import settings
from app.api.v1 import api_router
from app.db.database import engine, Base

# Настройка логирования
logging.basicConfig(
    level=getattr(logging, settings.LOG_LEVEL),
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Создание FastAPI приложения
app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description="API для анализа калорий и питательности еды с помощью AI",
    docs_url="/docs",
    redoc_url="/redoc"
)

# Настройка CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Подключение роутеров
app.include_router(api_router)

# Статические файлы (загруженные изображения)
app.mount("/uploads", StaticFiles(directory=settings.UPLOAD_DIR), name="uploads")


@app.on_event("startup")
async def startup_event():
    """Событие при запуске приложения"""
    logger.info(f"Запуск {settings.APP_NAME} v{settings.APP_VERSION}")

    # Создание таблиц в БД
    try:
        Base.metadata.create_all(bind=engine)
        logger.info("Таблицы базы данных созданы успешно")
    except Exception as e:
        logger.error(f"Ошибка при создании таблиц БД: {e}")


@app.on_event("shutdown")
async def shutdown_event():
    """Событие при остановке приложения"""
    logger.info(f"Остановка {settings.APP_NAME}")


@app.get("/")
async def root():
    """Корневой эндпоинт"""
    return {
        "app": settings.APP_NAME,
        "version": settings.APP_VERSION,
        "status": "running",
        "docs": "/docs"
    }


@app.get("/health")
async def health_check():
    """Проверка здоровья приложения"""
    return {"status": "healthy"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=settings.DEBUG
    )
