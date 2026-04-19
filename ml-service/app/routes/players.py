from fastapi import APIRouter, HTTPException
from app.database import get_players_stats_from_db
from app.logger import ml_logger

router = APIRouter(tags=["Players"])

@router.get("/players/stats")
def get_players_stats():
    try:
        return get_players_stats_from_db()
    except Exception as e:
        ml_logger.error(f"Failed to get players stats: {e}")
        raise HTTPException(status_code=500, detail=str(e))