"""
Pydantic схемы для статистики
"""
from pydantic import BaseModel
from datetime import date
from typing import List


class DailyStatsResponse(BaseModel):
    """Схема ответа с дневной статистикой"""
    date: date
    total_calories: float
    total_proteins: float
    total_fats: float
    total_carbs: float
    meals_count: int

    class Config:
        from_attributes = True


class StatsRangeResponse(BaseModel):
    """Схема ответа со статистикой за период"""
    daily_stats: List[DailyStatsResponse]
    period_total: DailyStatsResponse
    average_per_day: DailyStatsResponse


class CalorieDataPoint(BaseModel):
    """Точка данных для графика калорий"""
    date: str
    calories: float


class MacroDataPoint(BaseModel):
    """Точка данных для графика БЖУ"""
    date: str
    proteins: float
    fats: float
    carbs: float


class ChartDataResponse(BaseModel):
    """Схема данных для графиков"""
    calorie_data: List[CalorieDataPoint]
    macro_data: List[MacroDataPoint]
