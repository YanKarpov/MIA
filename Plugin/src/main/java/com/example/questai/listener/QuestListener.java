package com.example.questai.listener;

import com.example.questai.QuestPlugin;
import com.example.questai.model.QuestProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

public class QuestListener implements Listener {

    private final QuestPlugin plugin;

    public QuestListener(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        QuestProgress progress = plugin.getActiveQuests().get(player.getUniqueId());
        if (progress == null) return;

        // Любой блок подходит
        if ("Break".equals(progress.getQuest().getType())) {
            progress.increment();

            // Обновляем ActionBar с прогрессом
            plugin.updateActionBar(player, progress);

            // Проверяем, выполнен ли квест
            plugin.checkCompletion(player, progress);
        }
    }
}