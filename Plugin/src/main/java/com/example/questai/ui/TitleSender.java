package com.example.questai.ui;

import org.bukkit.entity.Player;

public class TitleSender {
    
    // Отправка тайтла о завершении квеста
    public static void sendCompletionTitle(Player player, int xpReward) {
        player.sendTitle(
                "✅ Quest Completed!",
                "Reward: " + xpReward + " XP",
                10, 70, 20
        );
    }
}