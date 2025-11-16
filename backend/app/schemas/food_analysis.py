"""
Pydantic схемы для анализа еды
"""
from pydantic import BaseModel
from datetime import datetime
from typing import List, Optional


class DishInfo(BaseModel):
    """Информация о блюде"""
    name: str
    portion_g: float
    calories: float
    proteins: float
    fats: float
    carbs: float


class Ingredient(BaseModel):
    """Ингредиент"""
    name: str
    amount: str


class NutritionTotal(BaseModel):
    """Общая питательная ценность"""
    calories: float
    proteins: float
    fats: float
    carbs: float


class FoodAnalysisResult(BaseModel):
    """Результат анализа от Claude"""
    dishes: List[DishInfo]
    total: NutritionTotal
    ingredients: List[Ingredient]
    health_tips: List[str]
    taste_tips: List[str]


class FoodAnalysisCreate(BaseModel):
    """Схема для создания анализа (только результат, фото передаётся отдельно)"""
    pass


class FoodAnalysisResponse(BaseModel):
    """Схема ответа с анализом еды"""
    id: int
    user_id: int
    image_url: str
    calories: float
    proteins: float
    fats: float
    carbs: float
    ingredients_json: List[Ingredient]
    health_tips_json: List[str]
    taste_tips_json: List[str]
    dishes_json: List[DishInfo]
    created_at: datetime

    class Config:
        from_attributes = True


class FoodAnalysisListResponse(BaseModel):
    """Схема ответа со списком анализов"""
    items: List[FoodAnalysisResponse]
    total: int
    page: int
    page_size: int
    pages: int
