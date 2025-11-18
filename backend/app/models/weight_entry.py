"""
Модель записей веса
"""
from sqlalchemy import Column, Integer, Float, Date, ForeignKey, Text
from sqlalchemy.orm import relationship
from datetime import date
from ..db.database import Base


class WeightEntry(Base):
    """Модель записи веса пользователя"""
    __tablename__ = "weight_entries"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False, index=True)
    weight_kg = Column(Float, nullable=False)  # Вес в килограммах
    date = Column(Date, default=date.today, nullable=False, index=True)
    note = Column(Text, nullable=True)  # Опциональная заметка

    # Связи
    user = relationship("User", back_populates="weight_entries")

    def __repr__(self):
        return f"<WeightEntry {self.id} - {self.weight_kg}kg on {self.date}>"
