"""
API эндпоинты для статистики
"""
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import func
from datetime import date, timedelta
from typing import List
from ...db.database import get_db
from ...models import DailyStats
from ...schemas import DailyStatsResponse, StatsRangeResponse, ChartDataResponse, CalorieDataPoint, MacroDataPoint
from ...core.security import get_current_user_id

router = APIRouter(prefix="/stats", tags=["Статистика"])


@router.get("/daily", response_model=DailyStatsResponse)
async def get_daily_stats(
    date_param: date = None,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """Статистика за день"""
    target_date = date_param or date.today()

    stats = db.query(DailyStats).filter(
        DailyStats.user_id == user_id,
        DailyStats.date == target_date
    ).first()

    if not stats:
        # Возвращаем пустую статистику
        return DailyStatsResponse(
            date=target_date,
            total_calories=0.0,
            total_proteins=0.0,
            total_fats=0.0,
            total_carbs=0.0,
            meals_count=0
        )

    return DailyStatsResponse.model_validate(stats)


@router.get("/weekly", response_model=StatsRangeResponse)
async def get_weekly_stats(
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """Статистика за неделю"""
    today = date.today()
    week_ago = today - timedelta(days=7)

    stats_list = db.query(DailyStats).filter(
        DailyStats.user_id == user_id,
        DailyStats.date >= week_ago,
        DailyStats.date <= today
    ).order_by(DailyStats.date).all()

    if not stats_list:
        return _empty_stats_response(week_ago, today)

    return _calculate_period_stats(stats_list, week_ago, today)


@router.get("/monthly", response_model=StatsRangeResponse)
async def get_monthly_stats(
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """Статистика за месяц"""
    today = date.today()
    month_ago = today - timedelta(days=30)

    stats_list = db.query(DailyStats).filter(
        DailyStats.user_id == user_id,
        DailyStats.date >= month_ago,
        DailyStats.date <= today
    ).order_by(DailyStats.date).all()

    if not stats_list:
        return _empty_stats_response(month_ago, today)

    return _calculate_period_stats(stats_list, month_ago, today)


@router.get("/chart", response_model=ChartDataResponse)
async def get_chart_data(
    days: int = 7,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """Данные для графиков"""
    today = date.today()
    start_date = today - timedelta(days=days)

    stats_list = db.query(DailyStats).filter(
        DailyStats.user_id == user_id,
        DailyStats.date >= start_date,
        DailyStats.date <= today
    ).order_by(DailyStats.date).all()

    calorie_data = [
        CalorieDataPoint(date=str(s.date), calories=s.total_calories)
        for s in stats_list
    ]

    macro_data = [
        MacroDataPoint(
            date=str(s.date),
            proteins=s.total_proteins,
            fats=s.total_fats,
            carbs=s.total_carbs
        )
        for s in stats_list
    ]

    return ChartDataResponse(calorie_data=calorie_data, macro_data=macro_data)


def _empty_stats_response(start_date: date, end_date: date) -> StatsRangeResponse:
    """Пустой ответ статистики"""
    empty_stat = DailyStatsResponse(
        date=start_date,
        total_calories=0.0,
        total_proteins=0.0,
        total_fats=0.0,
        total_carbs=0.0,
        meals_count=0
    )
    return StatsRangeResponse(
        daily_stats=[],
        period_total=empty_stat,
        average_per_day=empty_stat
    )


def _calculate_period_stats(stats_list: List[DailyStats], start_date: date, end_date: date) -> StatsRangeResponse:
    """Расчёт статистики за период"""
    total_calories = sum(s.total_calories for s in stats_list)
    total_proteins = sum(s.total_proteins for s in stats_list)
    total_fats = sum(s.total_fats for s in stats_list)
    total_carbs = sum(s.total_carbs for s in stats_list)
    total_meals = sum(s.meals_count for s in stats_list)
    days_count = len(stats_list) or 1

    period_total = DailyStatsResponse(
        date=start_date,
        total_calories=total_calories,
        total_proteins=total_proteins,
        total_fats=total_fats,
        total_carbs=total_carbs,
        meals_count=total_meals
    )

    average = DailyStatsResponse(
        date=start_date,
        total_calories=total_calories / days_count,
        total_proteins=total_proteins / days_count,
        total_fats=total_fats / days_count,
        total_carbs=total_carbs / days_count,
        meals_count=total_meals // days_count
    )

    return StatsRangeResponse(
        daily_stats=[DailyStatsResponse.model_validate(s) for s in stats_list],
        period_total=period_total,
        average_per_day=average
    )
