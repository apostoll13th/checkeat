"""
Pydantic схемы для пользователя
"""
from pydantic import BaseModel, EmailStr
from datetime import datetime
from typing import Optional, Dict


class UserBase(BaseModel):
    """Базовая схема пользователя"""
    email: EmailStr
    name: str


class UserCreate(UserBase):
    """Схема для создания пользователя"""
    password: str


class UserLogin(BaseModel):
    """Схема для входа"""
    email: EmailStr
    password: str


class UserResponse(UserBase):
    """Схема ответа с пользователем"""
    id: int
    created_at: datetime
    settings_json: Dict = {}

    class Config:
        from_attributes = True


class TokenResponse(BaseModel):
    """Схема ответа с токенами"""
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    user: UserResponse
