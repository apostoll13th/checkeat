"""
Модель анализа еды
"""
from sqlalchemy import Column, Integer, String, Float, DateTime, JSON, ForeignKey
from sqlalchemy.orm import relationship
from datetime import datetime
from ..db.database import Base


class FoodAnalysis(Base):
    """Модель анализа еды"""
    __tablename__ = "food_analyses"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False, index=True)
    image_url = Column(String, nullable=False)

    # Питательная информация
    calories = Column(Float, nullable=False)
    proteins = Column(Float, nullable=False)
    fats = Column(Float, nullable=False)
    carbs = Column(Float, nullable=False)

    # Детальная информация в JSON
    ingredients_json = Column(JSON, default=[])  # Список ингредиентов
    health_tips_json = Column(JSON, default=[])  # Советы по здоровью
    taste_tips_json = Column(JSON, default=[])  # Советы по вкусу
    dishes_json = Column(JSON, default=[])  # Информация о блюдах

    created_at = Column(DateTime, default=datetime.utcnow, nullable=False, index=True)

    # Связи
    user = relationship("User", back_populates="food_analyses")
    notes = relationship("Note", back_populates="analysis", cascade="all, delete-orphan")

    def __repr__(self):
        return f"<FoodAnalysis {self.id} - {self.calories} kcal>"
