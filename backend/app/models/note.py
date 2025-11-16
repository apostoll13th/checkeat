"""
Модель заметки
"""
from sqlalchemy import Column, Integer, String, Text, DateTime, ARRAY, ForeignKey
from sqlalchemy.orm import relationship
from datetime import datetime
from ..db.database import Base


class Note(Base):
    """Модель заметки"""
    __tablename__ = "notes"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False, index=True)
    analysis_id = Column(Integer, ForeignKey("food_analyses.id"), nullable=True, index=True)

    title = Column(String, nullable=False)
    content = Column(Text, nullable=False)
    tags = Column(ARRAY(String), default=[], index=True)

    created_at = Column(DateTime, default=datetime.utcnow, nullable=False, index=True)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    # Связи
    user = relationship("User", back_populates="notes")
    analysis = relationship("FoodAnalysis", back_populates="notes")

    def __repr__(self):
        return f"<Note {self.id} - {self.title}>"
