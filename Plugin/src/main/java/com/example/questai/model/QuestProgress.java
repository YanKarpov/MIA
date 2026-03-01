package com.example.questai.model;

public class QuestProgress {
    private final Quest quest;
    private int current;

    public QuestProgress(Quest quest) {
        this.quest = quest;
        this.current = 0;
    }

    public Quest getQuest() { return quest; }
    public int getCurrent() { return current; }
    public void increment() { current++; }
}