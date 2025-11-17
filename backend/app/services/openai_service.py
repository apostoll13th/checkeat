"""
Сервис для работы с OpenAI ChatGPT Vision API (GPT-4 Vision)
"""
import base64
import json
import logging
from typing import Dict
from openai import OpenAI
from ..core.config import settings
from ..schemas.food_analysis import FoodAnalysisResult, DishInfo, Ingredient, NutritionTotal

logger = logging.getLogger(__name__)


class OpenAIVisionService:
    """Сервис для анализа еды через ChatGPT Vision (GPT-4 Vision)"""

    def __init__(self):
        self.client = OpenAI(api_key=settings.OPENAI_API_KEY)
        self.model = settings.OPENAI_MODEL

    def _encode_image(self, image_path: str) -> str:
        """Кодирование изображения в base64"""
        with open(image_path, "rb") as image_file:
            return base64.b64encode(image_file.read()).decode("utf-8")

    def _get_analysis_prompt(self) -> str:
        """Получение промпта для анализа еды"""
        return """Проанализируй это фото еды и верни детальную информацию в JSON формате:

1) Определи все видимые блюда и продукты
2) Оцени размеры порций в граммах
3) Рассчитай для каждого блюда и общее: калории (ккал), белки (г), жиры (г), углеводы (г)
4) Перечисли все ингредиенты с количеством
5) Дай 3-5 конкретных советов как сделать это блюдо полезнее
6) Дай 3-5 конкретных советов как улучшить вкус

Формат ответа (СТРОГО JSON, без дополнительного текста):
{
  "dishes": [
    {
      "name": "название блюда",
      "portion_g": 250.0,
      "calories": 450.0,
      "proteins": 25.0,
      "fats": 18.0,
      "carbs": 35.0
    }
  ],
  "total": {
    "calories": 450.0,
    "proteins": 25.0,
    "fats": 18.0,
    "carbs": 35.0
  },
  "ingredients": [
    {"name": "курица", "amount": "150г"},
    {"name": "рис", "amount": "100г"}
  ],
  "health_tips": [
    "Добавьте больше овощей для клетчатки",
    "Уменьшите количество масла при приготовлении"
  ],
  "taste_tips": [
    "Добавьте свежих трав для аромата",
    "Попробуйте приправы: куркуму и паприку"
  ]
}

Будь максимально точным в расчётах. Если на фото несколько блюд, рассчитай каждое отдельно и суммируй в total."""

    async def analyze_food_image(self, image_path: str) -> FoodAnalysisResult:
        """
        Анализ изображения еды через ChatGPT Vision (GPT-4 Vision)

        Args:
            image_path: Путь к изображению

        Returns:
            FoodAnalysisResult: Результат анализа
        """
        try:
            # Кодируем изображение в base64
            base64_image = self._encode_image(image_path)

            # Определяем MIME тип изображения
            mime_type = "image/jpeg"
            if image_path.lower().endswith(".png"):
                mime_type = "image/png"
            elif image_path.lower().endswith(".webp"):
                mime_type = "image/webp"
            elif image_path.lower().endswith(".gif"):
                mime_type = "image/gif"

            # Отправляем запрос в ChatGPT Vision
            logger.info(f"Отправка запроса в ChatGPT Vision для анализа {image_path}")

            response = self.client.chat.completions.create(
                model=self.model,
                messages=[
                    {
                        "role": "user",
                        "content": [
                            {
                                "type": "text",
                                "text": self._get_analysis_prompt()
                            },
                            {
                                "type": "image_url",
                                "image_url": {
                                    "url": f"data:{mime_type};base64,{base64_image}",
                                    "detail": "high"  # Высокое качество анализа
                                }
                            }
                        ]
                    }
                ],
                max_tokens=2048,
                temperature=0.3,  # Низкая температура для более точных расчётов
            )

            # Извлекаем текст ответа
            response_text = response.choices[0].message.content
            logger.info(f"Получен ответ от ChatGPT Vision: {response_text[:200]}...")

            # Парсим JSON из ответа
            try:
                result_dict = json.loads(response_text)
            except json.JSONDecodeError:
                # Пытаемся извлечь JSON из текста (на случай если есть обрамляющий текст)
                start_idx = response_text.find('{')
                end_idx = response_text.rfind('}') + 1
                if start_idx != -1 and end_idx != 0:
                    json_str = response_text[start_idx:end_idx]
                    result_dict = json.loads(json_str)
                else:
                    raise ValueError("Не удалось распарсить ответ от ChatGPT Vision")

            # Преобразуем в Pydantic модель
            result = self._parse_result(result_dict)

            logger.info(f"Анализ завершён успешно: {result.total.calories} ккал")
            return result

        except Exception as e:
            logger.error(f"Ошибка при анализе изображения через ChatGPT Vision: {str(e)}", exc_info=True)
            raise

    def _parse_result(self, data: Dict) -> FoodAnalysisResult:
        """Преобразование словаря в FoodAnalysisResult"""
        # Парсим блюда
        dishes = [DishInfo(**dish) for dish in data.get("dishes", [])]

        # Парсим общие данные
        total = NutritionTotal(**data.get("total", {}))

        # Парсим ингредиенты
        ingredients = [Ingredient(**ing) for ing in data.get("ingredients", [])]

        # Советы
        health_tips = data.get("health_tips", [])
        taste_tips = data.get("taste_tips", [])

        return FoodAnalysisResult(
            dishes=dishes,
            total=total,
            ingredients=ingredients,
            health_tips=health_tips,
            taste_tips=taste_tips,
        )
