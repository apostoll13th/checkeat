"""
Модель ежедневной статистики
"""
from sqlalchemy import Column, Integer, Float, Date, ForeignKey, UniqueConstraint
from sqlalchemy.orm import relationship
from datetime import date
from ..db.database import Base


class DailyStats(Base):
    """Модель ежедневной статистики питания"""
    __tablename__ = "daily_stats"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False, index=True)
    date = Column(Date, default=date.today, nullable=False, index=True)

    # Суммарные значения за день
    total_calories = Column(Float, default=0.0)
    total_proteins = Column(Float, default=0.0)
    total_fats = Column(Float, default=0.0)
    total_carbs = Column(Float, default=0.0)
    meals_count = Column(Integer, default=0)

    # Связи
    user = relationship("User", back_populates="daily_stats")

    # Уникальный индекс: один user_id может иметь только одну запись на дату
    __table_args__ = (UniqueConstraint('user_id', 'date', name='_user_date_uc'),)

    def __repr__(self):
        return f"<DailyStats {self.user_id} - {self.date} - {self.total_calories} kcal>"
