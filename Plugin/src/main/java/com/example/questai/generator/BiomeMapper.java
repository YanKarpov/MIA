package com.example.questai.generator;

import org.bukkit.block.Biome;
import java.util.HashMap;
import java.util.Map;

public class BiomeMapper {
    private static final Map<Biome, String> CATEGORY = new HashMap<>();
    
    static {
        // Равнины
        CATEGORY.put(Biome.PLAINS, "plains");
        CATEGORY.put(Biome.SUNFLOWER_PLAINS, "plains");
        
        // Пустыня
        CATEGORY.put(Biome.DESERT, "desert");
        
        // Леса
        CATEGORY.put(Biome.FOREST, "forest");
        CATEGORY.put(Biome.BIRCH_FOREST, "forest");
        CATEGORY.put(Biome.DARK_FOREST, "forest");
        
        // Океаны
        CATEGORY.put(Biome.OCEAN, "ocean");
        CATEGORY.put(Biome.DEEP_OCEAN, "ocean");
        
        // Снежные
        CATEGORY.put(Biome.SNOWY_PLAINS, "snowy");
        CATEGORY.put(Biome.SNOWY_TAIGA, "snowy");
        
        // Болота
        CATEGORY.put(Biome.SWAMP, "swamp");
        
        // Незер
        CATEGORY.put(Biome.NETHER_WASTES, "nether");
        CATEGORY.put(Biome.CRIMSON_FOREST, "nether");
        CATEGORY.put(Biome.WARPED_FOREST, "nether");
        CATEGORY.put(Biome.SOUL_SAND_VALLEY, "nether");
        
        // Энд
        CATEGORY.put(Biome.THE_END, "end");
        CATEGORY.put(Biome.END_HIGHLANDS, "end");
        
        // Джунгли
        CATEGORY.put(Biome.JUNGLE, "jungle");
        CATEGORY.put(Biome.BAMBOO_JUNGLE, "jungle");
        
        // Грибные
        CATEGORY.put(Biome.MUSHROOM_FIELDS, "mushroom");
    }
    
    public static String getCategory(Biome biome) {
        return CATEGORY.getOrDefault(biome, "plains");
    }
}