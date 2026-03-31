package com.example.questai.ml;

import com.google.gson.annotations.SerializedName;

public class PlayerDTO {

    private int deaths;
    private int kills;

    @SerializedName("success_rate")
    private double successRate;

    public PlayerDTO(int deaths, int kills, double successRate) {
        this.deaths = deaths;
        this.kills = kills;
        this.successRate = successRate;
    }

    public int getDeaths() { return deaths; }
    public int getKills() { return kills; }

    @SerializedName("success_rate")
    public double getSuccessRate() {
        return successRate;
    }
}