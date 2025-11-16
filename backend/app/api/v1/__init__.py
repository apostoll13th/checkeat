"""
API v1 роутеры
"""
from fastapi import APIRouter
from .auth import router as auth_router
from .food_analysis import router as food_analysis_router
from .notes import router as notes_router
from .stats import router as stats_router

# Создание главного роутера API v1
api_router = APIRouter(prefix="/api/v1")

# Подключение роутеров
api_router.include_router(auth_router)
api_router.include_router(food_analysis_router)
api_router.include_router(notes_router)
api_router.include_router(stats_router)

__all__ = ["api_router"]
