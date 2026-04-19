package com.example.questai.service;

import com.example.questai.generator.QuestGenerator;
import com.example.questai.model.Player;
import com.example.questai.model.Quest;
import com.example.questai.model.QuestCandidate;
import com.example.questai.ml.QuestDTO;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class CandidateGenerator {
    
    private static final int DEFAULT_COUNT = 5;
    
    public List<QuestCandidate> generate(Player gamePlayer, Location location) {
        return generate(gamePlayer, location, DEFAULT_COUNT);
    }
    
    public List<QuestCandidate> generate(Player gamePlayer, Location location, int count) {
        List<QuestCandidate> candidates = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Quest quest = QuestGenerator.generateQuest(gamePlayer, location);
            QuestDTO dto = new QuestDTO(quest.getAmount());
            candidates.add(new QuestCandidate(quest, dto));
        }
        
        return candidates;
    }
    
    public void logCandidates(List<QuestCandidate> candidates) {
        System.out.println("--- CANDIDATES ---");
        for (int i = 0; i < candidates.size(); i++) {
            Quest q = candidates.get(i).getQuest();
            System.out.println("Candidate " + i + ": " + 
                               "type=" + q.getType() + 
                               ", target=" + q.getTarget() +
                               ", amount=" + q.getAmount() + 
                               ", reward=" + q.getReward());
        }
    }
}