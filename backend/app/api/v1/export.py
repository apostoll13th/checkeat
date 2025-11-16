"""
API эндпоинты для экспорта данных
"""
from fastapi import APIRouter, Depends, HTTPException
from fastapi.responses import StreamingResponse
from sqlalchemy.orm import Session
from datetime import datetime, date, timedelta
from typing import Optional
import io
import csv
from ...db.database import get_db
from ...models import FoodAnalysis, Note, DailyStats
from ...core.security import get_current_user_id

router = APIRouter(prefix="/export", tags=["Экспорт"])


@router.get("/csv/history")
async def export_history_csv(
    start_date: Optional[str] = None,
    end_date: Optional[str] = None,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """Экспорт истории анализов в CSV"""
    # Построение запроса
    query = db.query(FoodAnalysis).filter(FoodAnalysis.user_id == user_id)

    # Фильтрация по датам
    if start_date:
        query = query.filter(FoodAnalysis.created_at >= datetime.fromisoformat(start_date))
    if end_date:
        query = query.filter(FoodAnalysis.created_at <= datetime.fromisoformat(end_date))

    analyses = query.order_by(FoodAnalysis.created_at.desc()).all()

    # Создание CSV
    output = io.StringIO()
    writer = csv.writer(output)

    # Заголовки
    writer.writerow([
        'Дата',
        'Время',
        'Калории (ккал)',
        'Белки (г)',
        'Жиры (г)',
        'Углеводы (г)',
        'Количество блюд',
        'Изображение'
    ])

    # Данные
    for analysis in analyses:
        writer.writerow([
            analysis.created_at.strftime('%Y-%m-%d'),
            analysis.created_at.strftime('%H:%M'),
            f'{analysis.calories:.1f}',
            f'{analysis.proteins:.1f}',
            f'{analysis.fats:.1f}',
            f'{analysis.carbs:.1f}',
            len(analysis.dishes_json),
            analysis.image_url
        ])

    output.seek(0)

    # Возврат файла
    return StreamingResponse(
        iter([output.getvalue()]),
        media_type="text/csv",
        headers={
            "Content-Disposition": f"attachment; filename=checkeat_history_{datetime.now().strftime('%Y%m%d')}.csv"
        }
    )


@router.get("/csv/stats")
async def export_stats_csv(
    days: int = 30,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """Экспорт статистики в CSV"""
    today = date.today()
    start_date = today - timedelta(days=days)

    stats = db.query(DailyStats).filter(
        DailyStats.user_id == user_id,
        DailyStats.date >= start_date,
        DailyStats.date <= today
    ).order_by(DailyStats.date).all()

    # Создание CSV
    output = io.StringIO()
    writer = csv.writer(output)

    # Заголовки
    writer.writerow([
        'Дата',
        'Калории (ккал)',
        'Белки (г)',
        'Жиры (г)',
        'Углеводы (г)',
        'Количество приёмов пищи'
    ])

    # Данные
    for stat in stats:
        writer.writerow([
            stat.date.strftime('%Y-%m-%d'),
            f'{stat.total_calories:.1f}',
            f'{stat.total_proteins:.1f}',
            f'{stat.total_fats:.1f}',
            f'{stat.total_carbs:.1f}',
            stat.meals_count
        ])

    output.seek(0)

    return StreamingResponse(
        iter([output.getvalue()]),
        media_type="text/csv",
        headers={
            "Content-Disposition": f"attachment; filename=checkeat_stats_{datetime.now().strftime('%Y%m%d')}.csv"
        }
    )


@router.get("/csv/notes")
async def export_notes_csv(
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """Экспорт заметок в CSV"""
    notes = db.query(Note).filter(Note.user_id == user_id)\
                          .order_by(Note.created_at.desc()).all()

    # Создание CSV
    output = io.StringIO()
    writer = csv.writer(output)

    # Заголовки
    writer.writerow([
        'Дата создания',
        'Дата изменения',
        'Название',
        'Содержание',
        'Теги',
        'Привязка к анализу'
    ])

    # Данные
    for note in notes:
        writer.writerow([
            note.created_at.strftime('%Y-%m-%d %H:%M'),
            note.updated_at.strftime('%Y-%m-%d %H:%M'),
            note.title,
            note.content.replace('\n', ' '),
            ', '.join(note.tags) if note.tags else '',
            f'Анализ #{note.analysis_id}' if note.analysis_id else 'Нет'
        ])

    output.seek(0)

    return StreamingResponse(
        iter([output.getvalue()]),
        media_type="text/csv",
        headers={
            "Content-Disposition": f"attachment; filename=checkeat_notes_{datetime.now().strftime('%Y%m%d')}.csv"
        }
    )
