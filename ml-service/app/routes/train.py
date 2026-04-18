from fastapi import APIRouter, HTTPException
from app.trainer import train_model
from app.logger import ml_logger

router = APIRouter(tags=["Training"])

@router.post("/train")
def train():
    ml_logger.info("Manual training requested")
    try:
        train_model()
        ml_logger.info("Training completed successfully")
        return {"status": "trained", "message": "Модель успешно обучена"}
    except Exception as e:
        ml_logger.error(f"Training failed: {e}")
        raise HTTPException(status_code=500, detail=f"Training failed: {str(e)}")