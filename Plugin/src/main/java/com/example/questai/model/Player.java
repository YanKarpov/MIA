package com.example.questai.model;

import com.example.questai.ml.PlayerDTO;

public class Player {
    private int id;
    private String uuid;
    private String name;
    private int deaths;
    private int kills;
    private double successRate;
    private int totalQuests;
    private int completedQuests;
    
    // ===== НОВЫЕ ПОЛЯ ДЛЯ АНАЛИЗА ПРЕДПОЧТЕНИЙ =====
    private String lastQuestType;
    private int consecutiveSuccesses;
    private String favoriteType;
    private String leastFavoriteType;
    private String preferredTarget;
    // =============================================

    // Пустой конструктор
    public Player() {}

    // Конструктор для нового игрока
    public Player(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.deaths = 0;
        this.kills = 0;
        this.successRate = 0.5;
        this.totalQuests = 0;
        this.completedQuests = 0;
        this.lastQuestType = null;
        this.consecutiveSuccesses = 0;
        this.favoriteType = null;
        this.leastFavoriteType = null;
        this.preferredTarget = null;
    }

    // Конструктор с полными данными
    public Player(int id, String uuid, String name, int deaths, int kills, 
                  int totalQuests, int completedQuests, double successRate) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.deaths = deaths;
        this.kills = kills;
        this.totalQuests = totalQuests;
        this.completedQuests = completedQuests;
        this.successRate = successRate;
        this.lastQuestType = null;
        this.consecutiveSuccesses = 0;
        this.favoriteType = null;
        this.leastFavoriteType = null;
        this.preferredTarget = null;
    }

    // Преобразование в PlayerDTO для отправки в ML сервис
    public PlayerDTO toDTO() {
        return new PlayerDTO(deaths, kills, successRate);
    }

    // Обновление процента успешности
    public void updateSuccessRate() {
        if (totalQuests > 0) {
            this.successRate = (double) completedQuests / totalQuests;
        } else {
            this.successRate = 0.5;
        }
    }

    // Добавление успешно выполненного квеста
    public void addCompletedQuest() {
        this.totalQuests++;
        this.completedQuests++;
        updateSuccessRate();
    }

    // Добавление проваленного квеста
    public void addFailedQuest() {
        this.totalQuests++;
        updateSuccessRate();
    }

    // ===== ГЕТТЕРЫ И СЕТТЕРЫ =====
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public int getTotalQuests() {
        return totalQuests;
    }

    public void setTotalQuests(int totalQuests) {
        this.totalQuests = totalQuests;
        updateSuccessRate();
    }

    public int getCompletedQuests() {
        return completedQuests;
    }

    public void setCompletedQuests(int completedQuests) {
        this.completedQuests = completedQuests;
        updateSuccessRate();
    }
    
    // ===== НОВЫЕ ГЕТТЕРЫ И СЕТТЕРЫ =====
    
    public String getLastQuestType() {
        return lastQuestType;
    }
    
    public void setLastQuestType(String lastQuestType) {
        this.lastQuestType = lastQuestType;
    }
    
    public int getConsecutiveSuccesses() {
        return consecutiveSuccesses;
    }
    
    public void setConsecutiveSuccesses(int consecutiveSuccesses) {
        this.consecutiveSuccesses = consecutiveSuccesses;
    }
    
    public String getFavoriteType() {
        return favoriteType;
    }
    
    public void setFavoriteType(String favoriteType) {
        this.favoriteType = favoriteType;
    }
    
    public String getLeastFavoriteType() {
        return leastFavoriteType;
    }
    
    public void setLeastFavoriteType(String leastFavoriteType) {
        this.leastFavoriteType = leastFavoriteType;
    }
    
    public String getPreferredTarget() {
        return preferredTarget;
    }
    
    public void setPreferredTarget(String preferredTarget) {
        this.preferredTarget = preferredTarget;
    }
    // =================================

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", deaths=" + deaths +
                ", kills=" + kills +
                ", successRate=" + successRate +
                ", totalQuests=" + totalQuests +
                ", completedQuests=" + completedQuests +
                ", lastQuestType='" + lastQuestType + '\'' +
                ", consecutiveSuccesses=" + consecutiveSuccesses +
                ", favoriteType='" + favoriteType + '\'' +
                ", leastFavoriteType='" + leastFavoriteType + '\'' +
                ", preferredTarget='" + preferredTarget + '\'' +
                '}';
    }
}