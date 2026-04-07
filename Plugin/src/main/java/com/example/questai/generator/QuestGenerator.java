package com.example.questai.generator;

import com.example.questai.model.Player;
import com.example.questai.model.Quest;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QuestGenerator {

    private static final Random rand = new Random();
    
    // Типы квестов
    private static final String[] TYPES = {"Break", "Kill", "Collect"};
    
    // Блоки для сломать (Break) по категориям сложности
    private static final Map<String, String[]> BREAK_TARGETS = new HashMap<>();
    static {
        BREAK_TARGETS.put("common", new String[]{"STONE", "COBBLESTONE", "DIRT", "SAND", "GRAVEL"});
        BREAK_TARGETS.put("uncommon", new String[]{"COAL_ORE", "IRON_ORE", "GRANITE", "DIORITE"});
        BREAK_TARGETS.put("rare", new String[]{"DIAMOND_ORE", "GOLD_ORE", "EMERALD_ORE"});
        BREAK_TARGETS.put("nether", new String[]{"NETHERRACK", "SOUL_SAND", "GLOWSTONE"});
        BREAK_TARGETS.put("end", new String[]{"END_STONE", "PURPUR_BLOCK"});
    }
    
    // Мобы для убить (Kill) по биомам
    private static final Map<String, String[]> KILL_TARGETS = new HashMap<>();
    static {
        KILL_TARGETS.put("plains", new String[]{"ZOMBIE", "SKELETON", "SPIDER", "CREEPER"});
        KILL_TARGETS.put("desert", new String[]{"HUSK", "PHANTOM", "SPIDER", "ZOMBIE"});
        KILL_TARGETS.put("forest", new String[]{"WOLF", "SPIDER", "ZOMBIE", "SKELETON"});
        KILL_TARGETS.put("ocean", new String[]{"DROWNED", "GUARDIAN", "SQUID"});
        KILL_TARGETS.put("snowy", new String[]{"STRAY", "POLAR_BEAR", "ZOMBIE"});
        KILL_TARGETS.put("swamp", new String[]{"WITCH", "SLIME", "ZOMBIE"});
        KILL_TARGETS.put("nether", new String[]{"PIGLIN", "ZOMBIFIED_PIGLIN", "GHAST", "BLAZE"});
        KILL_TARGETS.put("end", new String[]{"ENDERMAN", "SHULKER"});
        KILL_TARGETS.put("jungle", new String[]{"PANDA", "OCELOT", "PARROT"});
        KILL_TARGETS.put("mushroom", new String[]{"MOOSHROOM"});
    }
    
    // Предметы для собрать (Collect)
    private static final Map<String, String[]> COLLECT_TARGETS = new HashMap<>();
    static {
        COLLECT_TARGETS.put("common", new String[]{"STICK", "FEATHER", "STRING", "ROTTEN_FLESH"});
        COLLECT_TARGETS.put("uncommon", new String[]{"IRON_INGOT", "GOLD_INGOT", "LEATHER"});
        COLLECT_TARGETS.put("rare", new String[]{"DIAMOND", "EMERALD", "NETHERITE_SCRAP"});
        COLLECT_TARGETS.put("nether", new String[]{"BLAZE_ROD", "GHAST_TEAR", "QUARTZ"});
        COLLECT_TARGETS.put("end", new String[]{"ENDER_PEARL", "CHORUS_FRUIT"});
    }
    
    // Соответствие биома Minecraft -> категория
    private static final Map<Biome, String> BIOME_CATEGORY = new HashMap<>();
    static {
        // Равнины
        BIOME_CATEGORY.put(Biome.PLAINS, "plains");
        BIOME_CATEGORY.put(Biome.SUNFLOWER_PLAINS, "plains");
        
        // Пустыня
        BIOME_CATEGORY.put(Biome.DESERT, "desert");
        
        // Леса
        BIOME_CATEGORY.put(Biome.FOREST, "forest");
        BIOME_CATEGORY.put(Biome.BIRCH_FOREST, "forest");
        BIOME_CATEGORY.put(Biome.DARK_FOREST, "forest");
        BIOME_CATEGORY.put(Biome.FLOWER_FOREST, "forest");
        
        // Океаны
        BIOME_CATEGORY.put(Biome.OCEAN, "ocean");
        BIOME_CATEGORY.put(Biome.DEEP_OCEAN, "ocean");
        
        // Снежные
        BIOME_CATEGORY.put(Biome.SNOWY_PLAINS, "snowy");
        BIOME_CATEGORY.put(Biome.SNOWY_TAIGA, "snowy");
        
        // Болота
        BIOME_CATEGORY.put(Biome.SWAMP, "swamp");
        
        // Незер
        BIOME_CATEGORY.put(Biome.NETHER_WASTES, "nether");
        BIOME_CATEGORY.put(Biome.CRIMSON_FOREST, "nether");
        BIOME_CATEGORY.put(Biome.WARPED_FOREST, "nether");
        BIOME_CATEGORY.put(Biome.SOUL_SAND_VALLEY, "nether");
        
        // Энд
        BIOME_CATEGORY.put(Biome.THE_END, "end");
        BIOME_CATEGORY.put(Biome.END_HIGHLANDS, "end");
        
        // Джунгли
        BIOME_CATEGORY.put(Biome.JUNGLE, "jungle");
        BIOME_CATEGORY.put(Biome.BAMBOO_JUNGLE, "jungle");
        
        // Грибные
        BIOME_CATEGORY.put(Biome.MUSHROOM_FIELDS, "mushroom");
    }

    public static Quest generateQuest() {
        return generateQuest(null, null);
    }
    
    public static Quest generateQuest(Player player, Location location) {
        Quest q = new Quest();
        q.setTarget("ANY");
        
        if (player == null || location == null) {
            String type = TYPES[rand.nextInt(TYPES.length)];
            q.setType(type);
            q.setTarget(getRandomTargetByType(type, null));
            q.setAmount(rand.nextInt(5) + 1);
            q.setReward(q.getAmount() * 5);
            return q;
        }
        
        // Получаем биом игрока
        Biome biome = location.getWorld().getBiome(location.getBlockX(), location.getBlockZ());
        String biomeCategory = getBiomeCategory(biome);
        
        String type = selectTypeByBiome(player, biomeCategory);
        q.setType(type);
        
        // Выбираем цель в зависимости от биома
        String target = selectTargetByBiome(type, biomeCategory, player);
        q.setTarget(target);
        
        int amount = calculateAmountWithBiome(player, type, biomeCategory);
        q.setAmount(amount);
        
        int reward = calculateReward(type, amount, target);
        q.setReward(reward);
        
        // Добавляем информацию о биоме в квест
        q.setBiome(biomeCategory);
        
        System.out.println("[QuestGenerator] Квест в биоме " + biomeCategory + 
                           ": " + type + " " + target + " x" + amount);
        
        return q;
    }
    
    private static String getBiomeCategory(Biome biome) {
        return BIOME_CATEGORY.getOrDefault(biome, "plains");
    }
    
    private static String selectTypeByBiome(Player player, String biomeCategory) {
        double successRate = player.getSuccessRate();
        
        // Адаптация под биом
        switch (biomeCategory) {
            case "nether":
                // В Незере чаще боевые квесты
                return rand.nextDouble() < 0.7 ? "Kill" : "Break";
            case "end":
                // В Энде тоже боевые
                return rand.nextDouble() < 0.6 ? "Kill" : "Collect";
            case "ocean":
                // В океане собирать сложно
                return rand.nextDouble() < 0.5 ? "Kill" : "Break";
            case "desert":
            case "snowy":
                // В экстремальных биомах проще ломать
                return rand.nextDouble() < 0.5 ? "Break" : "Collect";
            default:
                // Стандартное распределение
                double r = rand.nextDouble();
                if (r < 0.34) return "Kill";
                if (r < 0.67) return "Collect";
                return "Break";
        }
    }
    
    private static String getRandomTargetByType(String type, String biomeCategory) {
        switch (type) {
            case "Break":
                String[] breakTargets = BREAK_TARGETS.getOrDefault(biomeCategory, BREAK_TARGETS.get("common"));
                return breakTargets[rand.nextInt(breakTargets.length)];
            case "Kill":
                String[] killTargets = KILL_TARGETS.getOrDefault(biomeCategory, KILL_TARGETS.get("plains"));
                return killTargets[rand.nextInt(killTargets.length)];
            case "Collect":
                String[] collectTargets = COLLECT_TARGETS.getOrDefault(biomeCategory, COLLECT_TARGETS.get("common"));
                return collectTargets[rand.nextInt(collectTargets.length)];
            default:
                return "ANY";
        }
    }
    
    private static String selectTargetByBiome(String type, String biomeCategory, Player player) {
        return getRandomTargetByType(type, biomeCategory);
    }
    
    private static int calculateAmountWithBiome(Player player, String type, String biomeCategory) {
        double successRate = player.getSuccessRate();
        int kills = player.getKills();
        
        // Множитель сложности в зависимости от биома
        double biomeMultiplier = getBiomeDifficulty(biomeCategory);
        
        int baseAmount;
        switch (type) {
            case "Break": baseAmount = 5; break;
            case "Kill": baseAmount = 3; break;
            case "Collect": baseAmount = 8; break;
            default: baseAmount = 5;
        }
        
        // Применяем множитель биома
        baseAmount = (int)(baseAmount * biomeMultiplier);
        
        // Адаптация под уровень игрока
        if (successRate > 0.7) {
            return baseAmount + rand.nextInt(15) + 10;
        } else if (successRate < 0.4) {
            return Math.max(1, baseAmount - rand.nextInt(5));
        } else {
            return baseAmount + rand.nextInt(10);
        }
    }
    
    private static double getBiomeDifficulty(String biomeCategory) {
        switch (biomeCategory) {
            case "nether": return 2.0;   // Незер сложнее
            case "end": return 2.5;      // Энд ещё сложнее
            case "desert": return 1.2;   // Пустыня чуть сложнее
            case "snowy": return 1.3;    // Снег сложнее
            case "ocean": return 1.5;    // Океан
            default: return 1.0;         // Обычные биомы
        }
    }
    
    private static int calculateReward(String type, int amount, String target) {
        // Редкие цели дают больше награды
        int rarityBonus = getTargetRarity(target);
        
        switch (type) {
            case "Kill": return (amount * 8) + rarityBonus;
            case "Collect": return (amount * 5) + rarityBonus;
            case "Break": return (amount * 6) + rarityBonus;
            default: return amount * 5;
        }
    }
    
    private static int getTargetRarity(String target) {
        if (target.contains("DIAMOND") || target.contains("EMERALD") || 
            target.contains("NETHERITE") || target.contains("BLAZE")) {
            return 50; // Очень редкие
        }
        if (target.contains("IRON") || target.contains("GOLD") || 
            target.contains("ENDERMAN") || target.contains("GHAST")) {
            return 25; // Редкие
        }
        if (target.contains("COAL") || target.contains("ZOMBIE") || 
            target.contains("STONE") || target.contains("STICK")) {
            return 5; // Обычные
        }
        return 10;
    }
}