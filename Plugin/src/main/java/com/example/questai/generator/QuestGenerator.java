package com.example.questai.generator;

import com.example.questai.model.Player;
import com.example.questai.model.Quest;
import org.bukkit.Location;
import org.bukkit.block.Biome;

import java.util.Random;

public class QuestGenerator {
    
    private static final Random RAND = new Random();
    
    public static Quest generateQuest(Player player, Location location) {
        if (player == null || location == null) {
            return generateFallbackQuest();
        }
        
        Biome biome = location.getWorld().getBiome(location.getBlockX(), location.getBlockZ());
        String biomeCategory = BiomeMapper.getCategory(biome);
        
        QuestType type = selectType(player, biomeCategory);
        String target = TargetProvider.getTarget(type, biomeCategory);
        int amount = calculateAmount(player, type, biomeCategory);
        int reward = calculateReward(type, amount, target);
        
        Quest quest = new Quest();
        quest.setType(type.name());
        quest.setTarget(target);
        quest.setAmount(amount);
        quest.setReward(reward);
        quest.setBiome(biomeCategory);
        
        return quest;
    }
    
    private static QuestType selectType(Player player, String biomeCategory) {
        double successRate = player.getSuccessRate();
        
        // Адаптация под биом
        switch (biomeCategory) {
            case "nether":
                return RAND.nextDouble() < 0.7 ? QuestType.KILL : QuestType.BREAK;
            case "end":
                return RAND.nextDouble() < 0.6 ? QuestType.KILL : QuestType.COLLECT;
            case "ocean":
                return RAND.nextDouble() < 0.5 ? QuestType.KILL : QuestType.BREAK;
            case "desert":
            case "snowy":
                return RAND.nextDouble() < 0.5 ? QuestType.BREAK : QuestType.COLLECT;
            default:
                double r = RAND.nextDouble();
                if (r < 0.34) return QuestType.KILL;
                if (r < 0.67) return QuestType.COLLECT;
                return QuestType.BREAK;
        }
    }
    
    private static int calculateAmount(Player player, QuestType type, String biomeCategory) {
        double successRate = player.getSuccessRate();
        double biomeMultiplier = getBiomeDifficulty(biomeCategory);
        
        int baseAmount;
        switch (type) {
            case BREAK:
                baseAmount = 5;
                break;
            case KILL:
                baseAmount = 3;
                break;
            case COLLECT:
                baseAmount = 8;
                break;
            default:
                baseAmount = 5;
        }
        
        baseAmount = (int) (baseAmount * biomeMultiplier);
        
        if (successRate > 0.7) {
            return baseAmount + RAND.nextInt(15) + 10;
        } else if (successRate < 0.4) {
            return Math.max(1, baseAmount - RAND.nextInt(5));
        } else {
            return baseAmount + RAND.nextInt(10);
        }
    }
    
    private static double getBiomeDifficulty(String biomeCategory) {
        switch (biomeCategory) {
            case "nether":
                return 2.0;
            case "end":
                return 2.5;
            case "desert":
                return 1.2;
            case "snowy":
                return 1.3;
            case "ocean":
                return 1.5;
            default:
                return 1.0;
        }
    }
    
    private static int calculateReward(QuestType type, int amount, String target) {
        int rarityBonus = getTargetRarity(target);
        
        switch (type) {
            case KILL:
                return (amount * 8) + rarityBonus;
            case COLLECT:
                return (amount * 5) + rarityBonus;
            case BREAK:
                return (amount * 6) + rarityBonus;
            default:
                return amount * 5;
        }
    }
    
    private static int getTargetRarity(String target) {
        if (target.contains("DIAMOND") || target.contains("EMERALD") ||
            target.contains("NETHERITE") || target.contains("BLAZE")) {
            return 50;
        }
        if (target.contains("IRON") || target.contains("GOLD") ||
            target.contains("ENDERMAN") || target.contains("GHAST")) {
            return 25;
        }
        if (target.contains("COAL") || target.contains("ZOMBIE") ||
            target.contains("STONE") || target.contains("STICK")) {
            return 5;
        }
        return 10;
    }
    
    private static Quest generateFallbackQuest() {
        Quest quest = new Quest();
        quest.setType(QuestType.KILL.name());
        quest.setTarget("ZOMBIE");
        quest.setAmount(5);
        quest.setReward(25);
        return quest;
    }
}