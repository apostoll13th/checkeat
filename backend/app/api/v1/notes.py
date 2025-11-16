"""
API эндпоинты для заметок
"""
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List, Optional
from ...db.database import get_db
from ...models import Note
from ...schemas import NoteCreate, NoteUpdate, NoteResponse, NoteListResponse
from ...core.security import get_current_user_id

router = APIRouter(prefix="/notes", tags=["Заметки"])


@router.post("", response_model=NoteResponse, status_code=status.HTTP_201_CREATED)
async def create_note(
    note_data: NoteCreate,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """Создание заметки"""
    note = Note(
        user_id=user_id,
        analysis_id=note_data.analysis_id,
        title=note_data.title,
        content=note_data.content,
        tags=note_data.tags
    )
    db.add(note)
    db.commit()
    db.refresh(note)
    return NoteResponse.model_validate(note)


@router.get("", response_model=NoteListResponse)
async def get_notes(
    page: int = 1,
    page_size: int = 20,
    search: Optional[str] = None,
    tags: Optional[str] = None,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """Получение списка заметок"""
    query = db.query(Note).filter(Note.user_id == user_id)

    # Фильтрация по поиску
    if search:
        query = query.filter(
            (Note.title.contains(search)) | (Note.content.contains(search))
        )

    # Фильтрация по тегам
    if tags:
        tag_list = tags.split(",")
        query = query.filter(Note.tags.overlap(tag_list))

    total = query.count()
    items = query.order_by(Note.updated_at.desc())\
                 .offset((page - 1) * page_size)\
                 .limit(page_size)\
                 .all()

    return NoteListResponse(
        items=[NoteResponse.model_validate(i) for i in items],
        total=total,
        page=page,
        page_size=page_size,
        pages=(total + page_size - 1) // page_size
    )


@router.get("/{note_id}", response_model=NoteResponse)
async def get_note(
    note_id: int,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """Получение конкретной заметки"""
    note = db.query(Note).filter(Note.id == note_id, Note.user_id == user_id).first()
    if not note:
        raise HTTPException(status_code=404, detail="Заметка не найдена")
    return NoteResponse.model_validate(note)


@router.put("/{note_id}", response_model=NoteResponse)
async def update_note(
    note_id: int,
    note_data: NoteUpdate,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """Обновление заметки"""
    note = db.query(Note).filter(Note.id == note_id, Note.user_id == user_id).first()
    if not note:
        raise HTTPException(status_code=404, detail="Заметка не найдена")

    if note_data.title is not None:
        note.title = note_data.title
    if note_data.content is not None:
        note.content = note_data.content
    if note_data.tags is not None:
        note.tags = note_data.tags

    db.commit()
    db.refresh(note)
    return NoteResponse.model_validate(note)


@router.delete("/{note_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_note(
    note_id: int,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """Удаление заметки"""
    note = db.query(Note).filter(Note.id == note_id, Note.user_id == user_id).first()
    if not note:
        raise HTTPException(status_code=404, detail="Заметка не найдена")

    db.delete(note)
    db.commit()
