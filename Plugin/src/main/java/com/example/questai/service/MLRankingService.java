package com.example.questai.service;

import com.example.questai.ml.*;
import java.util.List;

public class MLRankingService {
    private final MLClient mlClient;
    
    public MLRankingService() {
        this.mlClient = new MLClient();
    }
    
    public List<RankResponse> rank(PlayerDTO playerDTO, List<QuestDTO> dtoList) {
        RankRequest request = new RankRequest(playerDTO, dtoList);
        return mlClient.rank(request);
    }
    
    public void logRequest(PlayerDTO playerDTO, org.bukkit.Location location) {
        System.out.println("--- ML REQUEST ---");
        System.out.println("Player: deaths=" + playerDTO.getDeaths() +
                ", kills=" + playerDTO.getKills() +
                ", successRate=" + playerDTO.getSuccessRate());
        System.out.println("Biome: " + location.getWorld().getBiome(location.getBlockX(), location.getBlockZ()));
    }
    
    public void logResponse(List<RankResponse> results) {
        System.out.println("--- ML RESPONSE ---");
        if (results == null || results.isEmpty()) {
            System.out.println("ML returned EMPTY or NULL");
        } else {
            for (RankResponse r : results) {
                System.out.println("Quest index: " + r.getQuestIndex() +
                        " | score: " + r.getScore());
            }
        }
    }
    
    public int getBestIndex(List<RankResponse> results) {
        if (results == null || results.isEmpty()) return -1;
        
        RankResponse best = null;
        for (RankResponse r : results) {
            if (best == null || r.getScore() > best.getScore()) {
                best = r;
            }
        }
        return best != null ? best.getQuestIndex() : -1;
    }
    
    public double getBestScore(List<RankResponse> results, int bestIndex) {
        if (results != null && bestIndex < results.size()) {
            return results.get(bestIndex).getScore();
        }
        return 0.5;
    }
}