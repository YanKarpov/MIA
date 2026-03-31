from fastapi import FastAPI
from pydantic import BaseModel
import numpy as np
from sklearn.ensemble import RandomForestClassifier

app = FastAPI()

model = RandomForestClassifier(n_estimators=50, random_state=42)

# Заглушка для обучения
X_train = np.array([
    [1, 50, 5, 0.9],
    [10, 10, 20, 0.2],
    [3, 30, 10, 0.7],
    [8, 5, 25, 0.3]
])

y_train = np.array([1, 0, 1, 0])

model.fit(X_train, y_train)


class Player(BaseModel):
    deaths: int
    kills: int
    success_rate: float

class Quest(BaseModel):
    amount: int

class RequestData(BaseModel):
    player: Player
    candidates: list[Quest]


@app.post("/rank")
def rank(data: RequestData):

    results = []

    for i, quest in enumerate(data.candidates):

        features = np.array([[
            data.player.deaths,
            data.player.kills,
            quest.amount,
            data.player.success_rate
        ]])

        score = model.predict_proba(features)[0][1]

        if data.player.deaths > 10:
            score *= 0.8

        if quest.amount > data.player.kills:
            score *= 0.7

        results.append({
            "questIndex": i,
            "score": float(score)
        })

    results.sort(key=lambda x: x["score"], reverse=True)

    return results