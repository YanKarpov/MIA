from fastapi import APIRouter, HTTPException
from app.trainer import train_model, force_train
from app.logger import ml_logger
from datetime import datetime

router = APIRouter(tags=["Training"])

@router.post("/train")
def train():
    ml_logger.info("Manual training requested")
    try:
        result = train_model()
        
        return {
            "status": result.get("status", "trained"),
            "message": result.get("message", "Модель успешно обучена"),
            "accuracy": result.get("accuracy", 0),
            "f1_score": result.get("f1_score", 0),
            "samples": result.get("samples", 0),
            "timestamp": datetime.now().isoformat()
        }
    except Exception as e:
        ml_logger.error(f"Training failed: {e}")
        raise HTTPException(status_code=500, detail=f"Training failed: {str(e)}")

@router.post("/train/force")
def train_force():
    """Принудительное обучение (игнорирует порог)"""
    ml_logger.info("Force training requested")
    try:
        result = force_train()
        
        return {
            "status": result.get("status", "trained"),
            "message": result.get("message", "Модель принудительно обучена"),
            "accuracy": result.get("accuracy", 0),
            "f1_score": result.get("f1_score", 0),
            "samples": result.get("samples", 0),
            "timestamp": datetime.now().isoformat()
        }
    except Exception as e:
        ml_logger.error(f"Force training failed: {e}")
        raise HTTPException(status_code=500, detail=f"Force training failed: {str(e)}")