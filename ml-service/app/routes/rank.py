from fastapi import APIRouter, HTTPException
from app.schemas import RankRequest, RankResponse
from app.model import predict
from app.logger import ml_logger

router = APIRouter(tags=["Prediction"])

@router.post("/rank", response_model=list[RankResponse])
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