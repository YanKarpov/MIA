package com.example.questai.model;

public class QuestProgress {

    private final int questId;
    private final Quest quest;

    private int current;

    public QuestProgress(int questId, Quest quest) {
        this.questId = questId;
        this.quest = quest;
        this.current = 0;
    }

    public int getQuestId() {
        return questId;
    }

    public Quest getQuest() {
        return quest;
    }

    public int getCurrent() {
        return current;
    }

    public void increment() {
        current++;
    }
}