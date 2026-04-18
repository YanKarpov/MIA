package com.example.questai.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TargetProvider {
    private static final Random RAND = new Random();
    
    private static final Map<String, String[]> BREAK_TARGETS = new HashMap<>();
    private static final Map<String, String[]> KILL_TARGETS = new HashMap<>();
    private static final Map<String, String[]> COLLECT_TARGETS = new HashMap<>();
    
    static {
        // Break targets
        BREAK_TARGETS.put("common", new String[]{"STONE", "COBBLESTONE", "DIRT", "SAND", "GRAVEL"});
        BREAK_TARGETS.put("uncommon", new String[]{"COAL_ORE", "IRON_ORE", "GRANITE", "DIORITE"});
        BREAK_TARGETS.put("rare", new String[]{"DIAMOND_ORE", "GOLD_ORE", "EMERALD_ORE"});
        BREAK_TARGETS.put("nether", new String[]{"NETHERRACK", "SOUL_SAND", "GLOWSTONE"});
        BREAK_TARGETS.put("end", new String[]{"END_STONE", "PURPUR_BLOCK"});
        
        // Kill targets
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
        
        // Collect targets
        COLLECT_TARGETS.put("common", new String[]{"STICK", "FEATHER", "STRING", "ROTTEN_FLESH"});
        COLLECT_TARGETS.put("uncommon", new String[]{"IRON_INGOT", "GOLD_INGOT", "LEATHER"});
        COLLECT_TARGETS.put("rare", new String[]{"DIAMOND", "EMERALD", "NETHERITE_SCRAP"});
        COLLECT_TARGETS.put("nether", new String[]{"BLAZE_ROD", "GHAST_TEAR", "QUARTZ"});
        COLLECT_TARGETS.put("end", new String[]{"ENDER_PEARL", "CHORUS_FRUIT"});
    }
    
    public static String getBreakTarget(String biomeCategory) {
        String[] targets = BREAK_TARGETS.getOrDefault(biomeCategory, BREAK_TARGETS.get("common"));
        return targets[RAND.nextInt(targets.length)];
    }
    
    public static String getKillTarget(String biomeCategory) {
        String[] targets = KILL_TARGETS.getOrDefault(biomeCategory, KILL_TARGETS.get("plains"));
        return targets[RAND.nextInt(targets.length)];
    }
    
    public static String getCollectTarget(String biomeCategory) {
        String[] targets = COLLECT_TARGETS.getOrDefault(biomeCategory, COLLECT_TARGETS.get("common"));
        return targets[RAND.nextInt(targets.length)];
    }
    
    public static String getTarget(QuestType type, String biomeCategory) {
        switch (type) {
            case BREAK:
                return getBreakTarget(biomeCategory);
            case KILL:
                return getKillTarget(biomeCategory);
            case COLLECT:
                return getCollectTarget(biomeCategory);
            default:
                return "STONE";
        }
    }
}