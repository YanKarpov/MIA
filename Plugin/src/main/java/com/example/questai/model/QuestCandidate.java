package com.example.questai.model;
import com.example.questai.ml.QuestDTO;

public class QuestCandidate {

    private Quest quest;
    private QuestDTO dto;

    public QuestCandidate(Quest quest, QuestDTO dto) {
        this.quest = quest;
        this.dto = dto;
    }

    public Quest getQuest() {
        return quest;
    }

    public QuestDTO getDto() {
        return dto;
    }
}