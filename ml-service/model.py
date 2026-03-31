import numpy as np
import os
import joblib
from sklearn.ensemble import RandomForestClassifier

MODEL_PATH = "model.pkl"

model = RandomForestClassifier(n_estimators=100, random_state=42)
is_trained = False


def load_model():
    global model, is_trained

    if os.path.exists(MODEL_PATH):
        model = joblib.load(MODEL_PATH)
        is_trained = True
        print("Model loaded")
    else:
        print("No saved model, using new one")


def save_model():
    joblib.dump(model, MODEL_PATH)


def predict(player, quest):
    global is_trained

    X = np.array([[
        player.deaths,
        player.kills,
        quest.amount,
        player.success_rate
    ]])

    if not is_trained:
        return 0.5

    return model.predict_proba(X)[0][1]


def fit(X, y):
    global model, is_trained

    model.fit(X, y)
    is_trained = True

    save_model()  