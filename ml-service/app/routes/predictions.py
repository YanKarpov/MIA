from fastapi import APIRouter, HTTPException, Query
from app.database import get_db_connection
from app.logger import ml_logger

router = APIRouter(tags=["Predictions"])

@router.get("/predictions/latest")
def get_latest_predictions(limit: int = Query(5, ge=1, le=20)):
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute("""
            SELECT 
                mp.candidate_index, 
                mp.predicted_score, 
                mp.was_selected,
                mp.type, 
                mp.target, 
                mp.amount, 
                mp.reward, 
                mp.quest_id
            FROM ml_predictions mp
            WHERE mp.quest_id = (
                SELECT quest_id FROM ml_predictions 
                ORDER BY created_at DESC LIMIT 1
            )
            ORDER BY mp.candidate_index
            LIMIT %s
        """, (limit,))
        rows = cur.fetchall()
        cur.close()
        conn.close()
        
        if not rows:
            return []
        
        result = []
        for row in rows:
            result.append({
                "index": row[0],
                "score": float(row[1]),
                "was_selected": row[2],
                "type": row[3],
                "target": row[4],
                "amount": row[5],
                "reward": row[6],
                "quest_id": row[7]
            })
        return result
    except Exception as e:
        ml_logger.error(f"Failed to get latest predictions: {e}")
        raise HTTPException(status_code=500, detail=str(e))