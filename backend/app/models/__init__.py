"""
Модели базы данных
"""
from .user import User
from .food_analysis import FoodAnalysis
from .note import Note
from .daily_stats import DailyStats
from .water_intake import WaterIntake
from .weight_entry import WeightEntry
from .goal import Goal
from .favorite import Favorite

__all__ = ["User", "FoodAnalysis", "Note", "DailyStats", "WaterIntake", "WeightEntry", "Goal", "Favorite"]
