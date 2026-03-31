from fastapi import FastAPI
from schemas import RankRequest, RankResponse
from model import predict, load_model
from trainer import train_model

app = FastAPI()

@app.get("/health")
def health():
    return {"status": "ok"}

@app.on_event("startup")
def startup():
    load_model()

@app.post("/train")
def train():
    train_model()
    return {"status": "trained"}

@app.post("/rank", response_model=list[RankResponse])
def rank(data: RankRequest):

    results = []

    for i, quest in enumerate(data.candidates):

        try:
            score = predict(data.player, quest)
        except Exception as e:
            print("PREDICT ERROR:", e)
            score = 0.5 

        results.append({
            "questIndex": i,
            "score": float(score)
        })

    results.sort(key=lambda x: x["score"], reverse=True)

    return results