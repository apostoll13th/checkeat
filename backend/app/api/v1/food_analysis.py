"""
API эндпоинты для анализа еды
"""
from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, status
from sqlalchemy.orm import Session
from datetime import date
from typing import List, Optional
from ...db.database import get_db
from ...models import FoodAnalysis, DailyStats
from ...schemas import FoodAnalysisResponse, FoodAnalysisListResponse
from ...core.security import get_current_user_id
from ...core.config import settings
from ...services.claude_service import ClaudeVisionService
from ...utils.file_utils import save_upload_file, get_file_url, delete_file

router = APIRouter(prefix="/analyze", tags=["Анализ еды"])
claude_service = ClaudeVisionService()


@router.post("", response_model=FoodAnalysisResponse, status_code=status.HTTP_201_CREATED)
async def analyze_food(
    file: UploadFile = File(...),
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """Анализ фото еды"""
    try:
        # Сохранение файла
        file_path = save_upload_file(file, user_id)

        # Анализ через Claude
        result = await claude_service.analyze_food_image(file_path)

        # Сохранение в БД
        analysis = FoodAnalysis(
            user_id=user_id,
            image_url=get_file_url(file_path),
            calories=result.total.calories,
            proteins=result.total.proteins,
            fats=result.total.fats,
            carbs=result.total.carbs,
            ingredients_json=[i.model_dump() for i in result.ingredients],
            health_tips_json=result.health_tips,
            taste_tips_json=result.taste_tips,
            dishes_json=[d.model_dump() for d in result.dishes]
        )
        db.add(analysis)

        # Обновление дневной статистики
        today = date.today()
        stats = db.query(DailyStats).filter(
            DailyStats.user_id == user_id,
            DailyStats.date == today
        ).first()

        if stats:
            stats.total_calories += result.total.calories
            stats.total_proteins += result.total.proteins
            stats.total_fats += result.total.fats
            stats.total_carbs += result.total.carbs
            stats.meals_count += 1
        else:
            stats = DailyStats(
                user_id=user_id,
                date=today,
                total_calories=result.total.calories,
                total_proteins=result.total.proteins,
                total_fats=result.total.fats,
                total_carbs=result.total.carbs,
                meals_count=1
            )
            db.add(stats)

        db.commit()
        db.refresh(analysis)

        return FoodAnalysisResponse.model_validate(analysis)

    except Exception as e:
        db.rollback()
        if 'file_path' in locals():
            delete_file(file_path)
        raise HTTPException(status_code=500, detail=f"Ошибка анализа: {str(e)}")


@router.get("/history", response_model=FoodAnalysisListResponse)
async def get_history(
    page: int = 1,
    page_size: int = 20,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """Получение истории анализов"""
    query = db.query(FoodAnalysis).filter(FoodAnalysis.user_id == user_id)
    total = query.count()

    items = query.order_by(FoodAnalysis.created_at.desc())\
                 .offset((page - 1) * page_size)\
                 .limit(page_size)\
                 .all()

    return FoodAnalysisListResponse(
        items=[FoodAnalysisResponse.model_validate(i) for i in items],
        total=total,
        page=page,
        page_size=page_size,
        pages=(total + page_size - 1) // page_size
    )


@router.get("/history/{analysis_id}", response_model=FoodAnalysisResponse)
async def get_analysis(
    analysis_id: int,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """Получение конкретного анализа"""
    analysis = db.query(FoodAnalysis).filter(
        FoodAnalysis.id == analysis_id,
        FoodAnalysis.user_id == user_id
    ).first()

    if not analysis:
        raise HTTPException(status_code=404, detail="Анализ не найден")

    return FoodAnalysisResponse.model_validate(analysis)


@router.delete("/history/{analysis_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_analysis(
    analysis_id: int,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """Удаление анализа"""
    analysis = db.query(FoodAnalysis).filter(
        FoodAnalysis.id == analysis_id,
        FoodAnalysis.user_id == user_id
    ).first()

    if not analysis:
        raise HTTPException(status_code=404, detail="Анализ не найден")

    # Удаление файла
    delete_file(analysis.image_url.replace("/uploads", settings.UPLOAD_DIR))

    db.delete(analysis)
    db.commit()
