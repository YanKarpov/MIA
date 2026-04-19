from fastapi import APIRouter, HTTPException
from app.database import get_db_connection
from app.logger import ml_logger

router = APIRouter(tags=["Settings"])

@router.get("/settings")
def get_settings():
    conn = get_db_connection()
    cur = conn.cursor()
    cur.execute("SELECT key, value FROM settings")
    rows = cur.fetchall()
    cur.close()
    conn.close()
    return {row[0]: row[1] for row in rows}

@router.post("/settings")
def update_settings(settings: dict):
    conn = get_db_connection()
    cur = conn.cursor()
    for key, value in settings.items():
        cur.execute("""
            INSERT INTO settings (key, value) VALUES (%s, %s)
            ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value, updated_at = NOW()
        """, (key, str(value)))
    conn.commit()
    cur.close()
    conn.close()
    ml_logger.info(f"Settings updated: {settings}")
    return {"status": "ok", "settings": settings}