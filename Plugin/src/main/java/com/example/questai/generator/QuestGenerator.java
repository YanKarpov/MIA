package com.example.questai.generator;

import com.example.questai.model.Player;
import com.example.questai.model.Quest;
import java.util.Random;

public class QuestGenerator {

    private static final Random rand = new Random();
    private static final String[] TYPES = {"Break", "Kill", "Collect"};

    public static Quest generateQuest() {
        return generateQuest(null);
    }

    public static Quest generateQuest(Player player) {
        Quest q = new Quest();
        q.setTarget("ANY");

        if (player == null) {
            String type = TYPES[rand.nextInt(TYPES.length)];
            q.setType(type);
            q.setAmount(rand.nextInt(5) + 1);
            q.setReward(q.getAmount() * 5);
            return q;
        }

        String type = selectTypeByPlayerStats(player);
        q.setType(type);
        int amount = calculateAmount(player, type);
        q.setAmount(amount);
        q.setReward(calculateReward(type, amount));
        
        return q;
    }

    private static String selectTypeByPlayerStats(Player player) {
        int kills = player.getKills();
        int deaths = player.getDeaths();
        double successRate = player.getSuccessRate();
        
        // НОВЫЙ ИГРОК - даём все типы равномерно
        if (successRate == 0.0 && kills == 0 && deaths == 0) {
            double r = rand.nextDouble();
            if (r < 0.34) return "Kill";
            if (r < 0.67) return "Collect";
            return "Break";
        }
        
        // Нет убийств - не даём Kill
        if (kills == 0) {
            return rand.nextBoolean() ? "Collect" : "Break";
        }
        
        // Много убийств - предпочитаем Kill
        if (kills > 50 && rand.nextDouble() < 0.7) {
            return "Kill";
        }
        
        // Много смертей - безопасные
        if (deaths > 20 && rand.nextDouble() < 0.6) {
            return "Collect";
        }
        
        // Стандартное распределение
        double r = rand.nextDouble();
        if (r < 0.34) return "Kill";
        if (r < 0.67) return "Collect";
        return "Break";
    }

    private static int calculateAmount(Player player, String type) {
        double successRate = player.getSuccessRate();
        int kills = player.getKills();
        
        // НОВЫЙ ИГРОК - только простые квесты
        if (successRate == 0.0 && kills == 0) {
            return rand.nextInt(3) + 1; // 1, 2 или 3
        }
        
        // Базовые значения
        int easy, medium, hard;
        switch (type) {
            case "Kill": easy = 2; medium = 8; hard = 20; break;
            case "Collect": easy = 3; medium = 12; hard = 30; break;
            case "Break": easy = 2; medium = 10; hard = 25; break;
            default: easy = 3; medium = 10; hard = 25;
        }
        
        if (successRate > 0.8) {
            return weightedChoice(easy, medium, hard, 0.1, 0.3, 0.6);
        } else if (successRate > 0.6) {
            return weightedChoice(easy, medium, hard, 0.2, 0.5, 0.3);
        } else if (successRate < 0.3) {
            return weightedChoice(easy, medium, easy, 0.7, 0.3, 0);
        } else if (successRate < 0.5) {
            return weightedChoice(easy, medium, hard, 0.5, 0.4, 0.1);
        } else {
            return weightedChoice(easy, medium, hard, 0.4, 0.4, 0.2);
        }
    }
    
    private static int weightedChoice(int easy, int medium, int hard, double pEasy, double pMedium, double pHard) {
        double r = rand.nextDouble();
        if (r < pEasy) return easy;
        if (r < pEasy + pMedium) return medium;
        return hard;
    }
    
    private static int calculateReward(String type, int amount) {
        switch (type) {
            case "Kill": return amount * 8;
            case "Collect": return amount * 5;
            case "Break": return amount * 6;
            default: return amount * 5;
        }
    }
}