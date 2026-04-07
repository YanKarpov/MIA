import numpy as np
import os
import joblib
from sklearn.ensemble import RandomForestClassifier
from datetime import datetime
from app.logger import ml_logger

MODEL_PATH = "model.pkl"

model = RandomForestClassifier(n_estimators=100, random_state=42)
is_trained = False
training_stats = {
    'samples_count': 0,
    'feature_importance': None,
    'last_training': None
}


def load_model():
    """Загружает обученную модель из файла"""
    global model, is_trained, training_stats
    
    if os.path.exists(MODEL_PATH):
        try:
            model = joblib.load(MODEL_PATH)
            is_trained = True
            ml_logger.info("Модель загружена из файла")
            
            if hasattr(model, 'feature_importances_'):
                training_stats['feature_importance'] = model.feature_importances_
                ml_logger.info(f"Важность признаков:")
                ml_logger.info(f"• смерти: {model.feature_importances_[0]:.3f}")
                ml_logger.info(f"• убийства: {model.feature_importances_[1]:.3f}")
                ml_logger.info(f"• количество: {model.feature_importances_[2]:.3f}")
                ml_logger.info(f"• успешность: {model.feature_importances_[3]:.3f}")
        except Exception as e:
            ml_logger.error(f"Ошибка загрузки модели: {e}")
            is_trained = False
    else:
        ml_logger.warning("Модель не найдена. Сначала обучите модель командой: python train_model.py")
        ml_logger.info("Пока используются эвристические предсказания")


def save_model():
    """Сохраняет модель в файл"""
    try:
        joblib.dump(model, MODEL_PATH)
        ml_logger.info("Модель сохранена в файл")
        return True
    except Exception as e:
        ml_logger.error(f"Ошибка сохранения модели: {e}")
        return False


def predict(player, quest):
    """Предсказывает вероятность успеха квеста"""
    global is_trained
    
    X = np.array([[
        player.deaths,
        player.kills,
        quest.amount,
        player.success_rate
    ]])
    
    if not is_trained:
        prob = heuristic_prediction(player, quest)
        ml_logger.debug(f"Эвристика: {prob:.3f} | "
                       f"смерти={player.deaths}, убийства={player.kills}, "
                       f"сложность={quest.amount}, успешность={player.success_rate:.3f}")
        return prob
    
    try:
        prob = model.predict_proba(X)[0][1]
        ml_logger.debug(f"Предсказание: {prob:.3f} | "
                       f"смерти={player.deaths}, убийства={player.kills}, "
                       f"сложность={quest.amount}, успешность={player.success_rate:.3f}")
        return prob
    except Exception as e:
        ml_logger.error(f"Ошибка предсказания: {e}")
        return 0.5


def heuristic_prediction(player, quest):
    """Простая эвристика для предсказания до обучения модели"""
    # Базовая сложность от amount (1-50 -> 0.9-0.1)
    difficulty = min(0.9, max(0.1, 1.0 - (quest.amount / 50.0)))
    
    # Корректировка от успешности игрока
    player_factor = (player.success_rate - 0.5) * 0.3
    
    # Корректировка для Kill квестов
    if hasattr(quest, 'type') and quest.type == 'Kill':
        if player.kills > 0:
            player_factor += 0.1
        else:
            player_factor -= 0.2
    
    prob = difficulty + player_factor
    prob = max(0.1, min(0.9, prob))
    
    return prob


def fit(X, y):
    """Обучает модель на переданных данных"""
    global model, is_trained, training_stats
    
    if len(X) == 0:
        ml_logger.warning("Нет данных для обучения")
        return False
    
    ml_logger.info("=" * 60)
    ml_logger.info(f"НАЧАЛО ОБУЧЕНИЯ МОДЕЛИ")
    ml_logger.info("=" * 60)
    ml_logger.info(f"Примеров: {len(X)}")
    ml_logger.info(f"Успешных: {sum(y)} ({sum(y)/len(X)*100:.1f}%)")
    ml_logger.info(f"Проваленных: {len(y)-sum(y)} ({(len(y)-sum(y))/len(y)*100:.1f}%)")
    
    # Создаём новую модель
    new_model = RandomForestClassifier(
        n_estimators=100,
        max_depth=10,
        min_samples_split=5,
        min_samples_leaf=2,
        random_state=42,
        n_jobs=-1
    )
    
    try:
        new_model.fit(X, y)
        
        # Обновляем глобальную модель
        model = new_model
        is_trained = True
        training_stats['samples_count'] = len(X)
        training_stats['feature_importance'] = model.feature_importances_
        training_stats['last_training'] = datetime.now()
        
        # Сохраняем модель
        save_model()
        
        # Логируем результаты
        accuracy = model.score(X, y)
        ml_logger.info("")
        ml_logger.info("РЕЗУЛЬТАТЫ ОБУЧЕНИЯ:")
        ml_logger.info(f"Точность на обучении: {accuracy:.3f} ({accuracy*100:.1f}%)")
        ml_logger.info("")
        ml_logger.info("ВАЖНОСТЬ ПРИЗНАКОВ:")
        ml_logger.info(f"💀 Смерти до квеста:      {model.feature_importances_[0]:.4f}")
        ml_logger.info(f"⚔️  Убийства до квеста:    {model.feature_importances_[1]:.4f}")
        ml_logger.info(f"📦 Сложность (amount):    {model.feature_importances_[2]:.4f}")
        ml_logger.info(f"📈 Успешность игрока:     {model.feature_importances_[3]:.4f}")
        
        features = ['смерти', 'убийства', 'сложность', 'успешность']
        most_important = features[np.argmax(model.feature_importances_)]
        ml_logger.info(f"")
        ml_logger.info(f"Самый важный признак: {most_important}")
        ml_logger.info("=" * 60)
        
        return True
        
    except Exception as e:
        ml_logger.error(f"Ошибка обучения: {e}")
        import traceback
        ml_logger.error(traceback.format_exc())
        return False


def get_model_info():
    """Возвращает информацию о модели"""
    if not is_trained:
        return {
            "status": "not_trained",
            "message": "Модель ещё не обучена",
            "heuristic": True
        }
    
    return {
        "status": "trained",
        "model_type": "RandomForestClassifier",
        "n_estimators": model.n_estimators,
        "max_depth": model.max_depth,
        "samples_count": training_stats['samples_count'],
        "feature_importance": {
            "deaths_before": float(training_stats['feature_importance'][0]),
            "kills_before": float(training_stats['feature_importance'][1]),
            "amount": float(training_stats['feature_importance'][2]),
            "success_rate_before": float(training_stats['feature_importance'][3])
        },
        "last_training": training_stats['last_training'].isoformat() if training_stats['last_training'] else None
    }