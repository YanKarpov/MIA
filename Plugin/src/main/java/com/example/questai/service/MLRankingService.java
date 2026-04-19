package com.example.questai.service;

import com.example.questai.ml.*;
import com.example.questai.model.QuestCandidate;
import org.bukkit.Location;
import java.util.ArrayList;
import java.util.List;

public class MLRankingService {
    private final MLClient mlClient;
    
    public MLRankingService() {
        this.mlClient = new MLClient();
    }
    
    public List<RankResponse> rankWithCandidates(List<QuestCandidate> candidates, 
                                                  com.example.questai.model.Player gamePlayer,
                                                  Location location) {
        List<QuestDTO> dtoList = candidates.stream()
                .map(QuestCandidate::getDto)
                .toList();
        
        PlayerDTO playerDTO = new PlayerDTO(gamePlayer.getDeaths(), gamePlayer.getKills(), gamePlayer.getSuccessRate());
        
        logRequest(playerDTO, location);
        List<RankResponse> results = mlClient.rank(new RankRequest(playerDTO, dtoList));
        logResponse(results);
        
        return results;
    }
    
    public void logRequest(PlayerDTO playerDTO, Location location) {
        System.out.println("--- ML REQUEST ---");
        System.out.println("Player: deaths=" + playerDTO.getDeaths() +
                ", kills=" + playerDTO.getKills() +
                ", successRate=" + playerDTO.getSuccessRate());
        if (location != null) {
            System.out.println("Biome: " + location.getWorld().getBiome(location.getBlockX(), location.getBlockZ()));
        }
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
    
    public double getScoreByIndex(List<RankResponse> results, int index) {
        if (results != null && index < results.size()) {
            return results.get(index).getScore();
        }
        return 0.5;
    }
    
    public int chooseBestWithThreshold(List<RankResponse> results, List<QuestCandidate> candidates, double threshold) {
        if (results == null || results.isEmpty()) {
            return chooseByAmount(candidates);
        }
        
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            if (results.get(i).getScore() >= threshold) {
                validIndices.add(i);
            }
        }
        
        System.out.println("Threshold filter: " + validIndices.size() + "/" + results.size() + 
                           " candidates passed (threshold=" + threshold + ")");
        
        if (validIndices.isEmpty()) {
            System.out.println("No candidates passed threshold, selecting best overall");
            return getBestIndex(results);
        }
        
        return getBestIndexByIndices(results, validIndices);
    }
    
    private int getBestIndexByIndices(List<RankResponse> results, List<Integer> indices) {
        if (indices.isEmpty()) return -1;
        
        int bestIndex = indices.get(0);
        double bestScore = results.get(bestIndex).getScore();
        
        for (int idx : indices) {
            if (results.get(idx).getScore() > bestScore) {
                bestScore = results.get(idx).getScore();
                bestIndex = idx;
            }
        }
        return bestIndex;
    }
    
    private int chooseByAmount(List<QuestCandidate> candidates) {
        if (candidates.isEmpty()) return -1;
        
        int easiestIndex = 0;
        for (int i = 1; i < candidates.size(); i++) {
            if (candidates.get(i).getQuest().getAmount() < candidates.get(easiestIndex).getQuest().getAmount()) {
                easiestIndex = i;
            }
        }
        System.out.println("Fallback: выбран лёгкий квест (amount=" + 
                           candidates.get(easiestIndex).getQuest().getAmount() + ")");
        return easiestIndex;
    }
}