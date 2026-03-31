package com.example.questai.listener;

import com.example.questai.QuestPlugin;
import com.example.questai.model.QuestProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class QuestListener implements Listener {

    private final QuestPlugin plugin;

    public QuestListener(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    // Ломание блоков
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        QuestProgress progress = plugin.getActiveQuests().get(player.getUniqueId());
        if (progress == null) return;

        if ("Break".equals(progress.getQuest().getType())) {
            progress.increment();
            plugin.updateActionBar(player, progress);
            plugin.checkCompletion(player, progress);
        }
    }

    // Убийство мобов
    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;

        Player player = event.getEntity().getKiller();
        QuestProgress progress = plugin.getActiveQuests().get(player.getUniqueId());
        if (progress == null) return;

        if ("Kill".equals(progress.getQuest().getType())) {
            progress.increment();
            plugin.updateActionBar(player, progress);
            plugin.checkCompletion(player, progress);
        }
    }

    // Сбор предметов
    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        QuestProgress progress = plugin.getActiveQuests().get(player.getUniqueId());
        if (progress == null) return;

        if ("Collect".equals(progress.getQuest().getType())) {
            progress.increment();
            plugin.updateActionBar(player, progress);
            plugin.checkCompletion(player, progress);
        }
    }

    // Смерть игрока = провал квеста
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        QuestProgress progress = plugin.getActiveQuests().get(player.getUniqueId());
        if (progress == null) return;

        try {
            plugin.getQuestService().failQuest(player, progress.getQuestId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        plugin.getActiveQuests().remove(player.getUniqueId());

        player.sendMessage("§cQuest failed! You died.");
    }
}