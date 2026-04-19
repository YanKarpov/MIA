from fastapi import FastAPI
from app.routes import health, quests, players, stats, database, train, rank, predictions, settings
from app.model import load_model
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

app.include_router(health.router)
app.include_router(quests.router)
app.include_router(players.router)
app.include_router(stats.router)
app.include_router(database.router)
app.include_router(settings.router)
app.include_router(train.router)
app.include_router(rank.router)
app.include_router(predictions.router)

@app.on_event("startup")
def startup():
    ml_logger.info("Starting Quest AI ML Service...")
    load_model()
    ml_logger.info("Service ready")