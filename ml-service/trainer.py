import numpy as np
from db import get_connection
from model import fit

THRESHOLD = 5  

last_train_count = 0


def get_records_count():
    conn = get_connection()
    cur = conn.cursor()

    cur.execute("""
        SELECT COUNT(*) FROM quests WHERE success IS NOT NULL
    """)

    count = cur.fetchone()[0]

    cur.close()
    conn.close()

    return count


def load_data():
    conn = get_connection()
    cur = conn.cursor()

    # Получаем общий success_rate для всех записей
    cur.execute("""
        SELECT 
            deaths, kills, amount, success,
            (SELECT AVG(CASE WHEN success THEN 1 ELSE 0 END) 
             FROM quests WHERE success IS NOT NULL) as success_rate
        FROM quests
        WHERE success IS NOT NULL
    """)

    rows = cur.fetchall()

    cur.close()
    conn.close()

    X = []
    y = []

    for deaths, kills, amount, success, success_rate in rows:
        X.append([deaths or 0, kills or 0, amount or 0, success_rate or 0])
        y.append(1 if success else 0)

    return np.array(X), np.array(y)


def train_model():
    global last_train_count

    current_count = get_records_count()

    if current_count < THRESHOLD:
        print(f"Not enough data: {current_count}/{THRESHOLD}")
        return

    if current_count - last_train_count < THRESHOLD:
        return

    X, y = load_data()

    if len(X) == 0:
        print("No data to train")
        return

    print(f"Training with {X.shape[1]} features: deaths, kills, amount, success_rate")
    fit(X, y)

    last_train_count = current_count

    print(f"Model retrained on {len(X)} samples")