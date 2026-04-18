package com.example.questai.generator;

public enum QuestType {
    BREAK("Сломать"),
    KILL("Убить"),
    COLLECT("Собрать");
    
    private final String displayName;
    
    QuestType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static QuestType fromString(String type) {
        for (QuestType t : values()) {
            if (t.name().equalsIgnoreCase(type)) {
                return t;
            }
        }
        return KILL;
    }
}