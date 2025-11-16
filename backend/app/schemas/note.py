"""
Pydantic схемы для заметок
"""
from pydantic import BaseModel
from datetime import datetime
from typing import List, Optional


class NoteBase(BaseModel):
    """Базовая схема заметки"""
    title: str
    content: str
    tags: List[str] = []


class NoteCreate(NoteBase):
    """Схема для создания заметки"""
    analysis_id: Optional[int] = None


class NoteUpdate(BaseModel):
    """Схема для обновления заметки"""
    title: Optional[str] = None
    content: Optional[str] = None
    tags: Optional[List[str]] = None


class NoteResponse(NoteBase):
    """Схема ответа с заметкой"""
    id: int
    user_id: int
    analysis_id: Optional[int] = None
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class NoteListResponse(BaseModel):
    """Схема ответа со списком заметок"""
    items: List[NoteResponse]
    total: int
    page: int
    page_size: int
    pages: int
