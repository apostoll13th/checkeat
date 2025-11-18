"""
Модель избранных блюд
"""
from sqlalchemy import Column, Integer, String, Float, DateTime, ForeignKey, JSON
from sqlalchemy.orm import relationship
from datetime import datetime
from ..db.database import Base


class Favorite(Base):
    """Модель избранного блюда/продукта"""
    __tablename__ = "favorites"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False, index=True)

    # Информация о блюде/продукте
    name = Column(String, nullable=False)  # Название
    calories = Column(Float, nullable=False)  # Калории
    proteins = Column(Float, nullable=False)  # Белки (г)
    fats = Column(Float, nullable=False)  # Жиры (г)
    carbs = Column(Float, nullable=False)  # Углеводы (г)
    portion_g = Column(Float, nullable=True)  # Размер порции (г)

    # Дополнительная информация
    ingredients_json = Column(JSON, default=list)  # Ингредиенты
    image_url = Column(String, nullable=True)  # URL изображения

    created_at = Column(DateTime, default=datetime.utcnow, nullable=False, index=True)

    # Связи
    user = relationship("User", back_populates="favorites")

    def __repr__(self):
        return f"<Favorite {self.id} - {self.name}>"
