from fastapi import APIRouter, HTTPException, Query
from app.database import get_db_table_data
from app.logger import ml_logger

router = APIRouter(tags=["Database"])

@router.get("/db/{table_name}")
def get_db_table(table_name: str, limit: int = Query(50, ge=1, le=200)):
    allowed = ["quests", "players", "ml_logs"]
    if table_name not in allowed:
        raise HTTPException(404, f"Table '{table_name}' not found")
    
    try:
        return get_db_table_data(table_name, limit)
    except Exception as e:
        ml_logger.error(f"Failed to get table {table_name}: {e}")
        raise HTTPException(500, detail=str(e))