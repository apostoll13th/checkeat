"""
Модель целей пользователя
"""
from sqlalchemy import Column, Integer, Float, Boolean, ForeignKey, DateTime
from sqlalchemy.orm import relationship
from datetime import datetime
from ..db.database import Base


class Goal(Base):
    """Модель цели пользователя (калории, макросы и т.д.)"""
    __tablename__ = "goals"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False, index=True)

    # Цели по питанию
    daily_calories = Column(Float, nullable=True)  # Цель калорий в день
    daily_proteins = Column(Float, nullable=True)  # Цель белков в день (г)
    daily_fats = Column(Float, nullable=True)  # Цель жиров в день (г)
    daily_carbs = Column(Float, nullable=True)  # Цель углеводов в день (г)
    daily_water_ml = Column(Float, nullable=True)  # Цель потребления воды (мл)

    # Цели по весу
    target_weight_kg = Column(Float, nullable=True)  # Целевой вес (кг)

    # Настройки
    is_active = Column(Boolean, default=True, nullable=False)  # Активна ли цель

    # Timestamps
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    # Связи
    user = relationship("User", back_populates="goals")

    def __repr__(self):
        return f"<Goal {self.id} - {self.daily_calories} kcal/day>"
