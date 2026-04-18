from fastapi import APIRouter, Query
from app.database import get_recent_quests_from_db
from app.logger import ml_logger

router = APIRouter(tags=["Quests"])

@router.get("/quests/recent")
def get_recent_quests(limit: int = Query(10, ge=1, le=100)):
    try:
        return get_recent_quests_from_db(limit)
    except Exception as e:
        ml_logger.error(f"Failed to get recent quests: {e}")
        raise HTTPException(status_code=500, detail=str(e))