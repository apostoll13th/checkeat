"""
Модель потребления воды
"""
from sqlalchemy import Column, Integer, Float, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from datetime import datetime
from ..db.database import Base


class WaterIntake(Base):
    """Модель записи потребления воды"""
    __tablename__ = "water_intakes"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False, index=True)
    amount_ml = Column(Float, nullable=False)  # Количество в миллилитрах
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False, index=True)

    # Связи
    user = relationship("User", back_populates="water_intakes")

    def __repr__(self):
        return f"<WaterIntake {self.id} - {self.amount_ml}ml>"
