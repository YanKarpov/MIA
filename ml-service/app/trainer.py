import numpy as np
from app.database import get_db_connection
from app.model import fit, model
import app.model as model_module
from app.logger import ml_logger
from sklearn.metrics import accuracy_score, f1_score

THRESHOLD = 5
last_train_count = 0


def get_records_count():
    """Получает количество завершённых квестов"""
    conn = get_db_connection()
    cur = conn.cursor()

    cur.execute("""
        SELECT COUNT(*) FROM quests WHERE status != 'IN_PROGRESS'
    """)

    count = cur.fetchone()[0]

    cur.close()
    conn.close()

    return count


def load_data():
    """Загружает данные для обучения из БД"""
    conn = get_db_connection()
    cur = conn.cursor()

    cur.execute("""
        SELECT 
            deaths_before,
            kills_before,
            amount,
            success_rate_before,
            CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END as completed
        FROM quests
        WHERE status != 'IN_PROGRESS'
        ORDER BY issued_at
    """)

    rows = cur.fetchall()

    cur.close()
    conn.close()

    if len(rows) == 0:
        ml_logger.warning("Нет данных для обучения")
        return np.array([]), np.array([])

    X = []
    y = []

    for deaths_before, kills_before, amount, success_rate_before, completed in rows:
        X.append([
            deaths_before or 0,
            kills_before or 0,
            amount or 0,
            success_rate_before or 0.5
        ])
        y.append(completed)

    return np.array(X), np.array(y)


def train_model():
    """Обучает модель если накопилось достаточно новых данных и возвращает метрики"""
    global last_train_count

    current_count = get_records_count()

    if current_count < THRESHOLD:
        ml_logger.info(f"Недостаточно данных: {current_count}/{THRESHOLD}")
        return {
            "status": "insufficient_data",
            "message": f"Need {THRESHOLD - current_count} more samples",
            "accuracy": 0,
            "f1_score": 0,
            "samples": current_count
        }

    new_data_count = current_count - last_train_count
    if new_data_count < THRESHOLD and last_train_count > 0:
        ml_logger.info(f"Новых данных недостаточно: {new_data_count}/{THRESHOLD}")
        return {
            "status": "insufficient_new_data",
            "message": f"Need {THRESHOLD - new_data_count} more new samples",
            "accuracy": 0,
            "f1_score": 0,
            "samples": current_count
        }

    X, y = load_data()

    if len(X) == 0:
        ml_logger.warning("Нет данных для обучения")
        return {
            "status": "no_data",
            "message": "No training data available",
            "accuracy": 0,
            "f1_score": 0,
            "samples": 0
        }

    ml_logger.info("")
    ml_logger.info("Начинаю обучение модели...")
    ml_logger.info(f"Всего записей: {len(X)}")
    ml_logger.info(f"Успешных: {sum(y)}/{len(X)} ({sum(y)/len(X)*100:.1f}%)")
    ml_logger.info(f"Признаки: смерти_до, убийства_до, сложность, успешность_до")

    # Обучаем модель
    fit(X, y)

    # Получаем актуальное состояние модели через модуль
    is_model_trained = model_module.is_trained
    current_model = model_module.model

    # Рассчитываем метрики
    if is_model_trained:
        y_pred = current_model.predict(X)
        accuracy = accuracy_score(y, y_pred)
        f1 = f1_score(y, y_pred, average='weighted')
        
        # Сохраняем метрики в лог
        ml_logger.info("")
        ml_logger.info("МЕТРИКИ МОДЕЛИ:")
        ml_logger.info(f"Точность (Accuracy): {accuracy:.3f} ({accuracy*100:.1f}%)")
        ml_logger.info(f"F1 Score: {f1:.3f}")
    else:
        accuracy = 0
        f1 = 0
        ml_logger.warning("Модель не обучилась")

    last_train_count = current_count

    ml_logger.info(f"Модель обучена на {len(X)} примерах")
    ml_logger.info(f"Новых примеров добавлено: {new_data_count if last_train_count > 0 else len(X)}")
    ml_logger.info("=" * 50)

    return {
        "status": "trained" if is_model_trained else "failed",
        "message": "Model trained successfully" if is_model_trained else "Training failed",
        "accuracy": accuracy,
        "f1_score": f1,
        "samples": len(X)
    }


def force_train():
    """Принудительное обучение модели (игнорирует порог новых данных) и возвращает метрики"""
    global last_train_count
    
    X, y = load_data()
    
    if len(X) == 0:
        ml_logger.warning("Нет данных для обучения")
        return {
            "status": "no_data",
            "message": "No training data available",
            "accuracy": 0,
            "f1_score": 0,
            "samples": 0
        }
    
    ml_logger.info("")
    ml_logger.info("Принудительное обучение модели...")
    ml_logger.info(f"   Всего записей: {len(X)}")
    
    fit(X, y)
    
    # Получаем актуальное состояние модели через модуль
    is_model_trained = model_module.is_trained
    current_model = model_module.model
    
    # Рассчитываем метрики
    if is_model_trained:
        y_pred = current_model.predict(X)
        accuracy = accuracy_score(y, y_pred)
        f1 = f1_score(y, y_pred, average='weighted')
    else:
        accuracy = 0
        f1 = 0
    
    last_train_count = get_records_count()
    
    ml_logger.info(f"Модель обучена на {len(X)} примерах")
    ml_logger.info(f"Accuracy: {accuracy:.3f}, F1 Score: {f1:.3f}")
    
    return {
        "status": "trained" if is_model_trained else "failed",
        "message": "Model trained successfully" if is_model_trained else "Training failed",
        "accuracy": accuracy,
        "f1_score": f1,
        "samples": len(X)
    }


def get_training_stats():
    """Возвращает статистику для отладки"""
    current_count = get_records_count()
    
    conn = get_db_connection()
    cur = conn.cursor()
    
    cur.execute("""
        SELECT 
            COUNT(*) as total,
            COUNT(*) FILTER (WHERE status = 'COMPLETED') as completed,
            MIN(amount) as min_amount,
            MAX(amount) as max_amount,
            AVG(amount) as avg_amount
        FROM quests
        WHERE status != 'IN_PROGRESS'
    """)
    
    stats = cur.fetchone()
    cur.close()
    conn.close()
    
    return {
        'total_quests': stats[0],
        'completed_quests': stats[1],
        'success_rate': stats[1] / stats[0] if stats[0] > 0 else 0,
        'min_amount': stats[2],
        'max_amount': stats[3],
        'avg_amount': stats[4],
        'last_train_count': last_train_count
    }


ml_logger.info("Trainer модуль загружен")
ml_logger.info(f"Текущее количество записей: {get_records_count()}")
ml_logger.info(f"Порог обучения: {THRESHOLD}")