from fastapi import APIRouter, HTTPException
from app.database import get_db_connection

router = APIRouter(tags=["Health"])

@router.get("/health")
def health():
    return {"status": "ok", "service": "Quest AI ML"}

@router.get("/db/health")
def db_health():
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute("SELECT 1")
        cur.close()
        conn.close()
        return {"status": "ok", "database": "postgres"}
    except Exception:
        raise HTTPException(status_code=503, detail="Database unavailable")

@router.get("/minecraft/status")
def minecraft_status():
    return {"status": "ok", "server": "minecraft"}