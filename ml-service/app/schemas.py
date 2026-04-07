from pydantic import BaseModel
from typing import List

class PlayerDTO(BaseModel):
    deaths: int
    kills: int
    success_rate: float

class QuestDTO(BaseModel):
    amount: int

class RankRequest(BaseModel):
    player: PlayerDTO
    candidates: List[QuestDTO]

class RankResponse(BaseModel):
    questIndex: int
    score: float