package com.example.questai.ui;

import com.example.questai.model.Quest;
import com.example.questai.model.QuestProgress;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class ActionBarUpdater {
    
    // Обновление action bar для игрока
    public static void update(Player player, QuestProgress progress) {
        Quest quest = progress.getQuest();
        String targetShort = MessageFormat.getShortTarget(quest);
        
        String progressText = progress.getCurrent() + "/" + quest.getAmount() + " " + targetShort;
        
        player.sendActionBar(Component.text(
                "📋 " + quest.getType() + ": " + progressText
        ).color(NamedTextColor.GREEN));
    }
}