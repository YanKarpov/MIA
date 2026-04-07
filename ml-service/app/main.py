from fastapi import FastAPI, HTTPException
from app.schemas import RankRequest, RankResponse
from app.model import predict, load_model
from app.trainer import train_model
from app.logger import ml_logger

app = FastAPI(
    title="MIA Service",
    description="Система машинного обучения для динамических квестов Minecraft",
    version="1.0.0",
    swagger_ui_parameters={
        "displayRequestDuration": True,
        "filter": True,
    }
)

@app.get("/health", tags=["Health"])
def health():
    ml_logger.debug("Health check requested")
    return {"status": "ok", "service": "Quest AI ML"}

@app.on_event("startup")
def startup():
    ml_logger.info("Starting Quest AI ML Service...")
    load_model()
    ml_logger.info("Service ready")

@app.post("/train", tags=["Training"])
def train():
    ml_logger.info("Manual training requested")
    try:
        train_model()
        ml_logger.info("Training completed successfully")
        return {"status": "trained", "message": "Модель успешно обучена"}
    except Exception as e:
        ml_logger.error(f"Training failed: {e}")
        raise HTTPException(status_code=500, detail=f"Training failed: {str(e)}")

@app.post("/rank", response_model=list[RankResponse], tags=["Prediction"])
def rank(data: RankRequest):
    ml_logger.info(f"Rank request: player deaths={data.player.deaths}, kills={data.player.kills}, success_rate={data.player.success_rate}")
    
    results = []
    for i, quest in enumerate(data.candidates):
        try:
            score = predict(data.player, quest)
            ml_logger.debug(f"Quest {i}: amount={quest.amount} -> score={score:.3f}")
        except Exception as e:
            ml_logger.error(f"Prediction error for quest {i}: {e}")
            score = 0.5

        results.append(RankResponse(questIndex=i, score=float(score)))

    results.sort(key=lambda x: x.score, reverse=True)
    if results:
        ml_logger.info(f"Rank result: best index={results[0].questIndex}, best score={results[0].score:.3f}")
    else:
        ml_logger.info("Rank result: no results")
        
    return results