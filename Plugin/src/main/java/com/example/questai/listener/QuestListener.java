package com.example.questai.listener;

import com.example.questai.QuestPlugin;
import com.example.questai.model.QuestProgress;
import com.example.questai.ui.ActionBarUpdater;
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

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        QuestProgress progress = plugin.getActiveQuests().get(player.getUniqueId());
        if (progress == null) return;

        String questType = progress.getQuest().getType();
        String questTarget = progress.getQuest().getTarget();
        String blockType = event.getBlock().getType().toString();
        
        if ("Break".equals(questType)) {
            if (questTarget == null || questTarget.equals("ANY") || blockType.equals(questTarget)) {
                progress.increment();
                ActionBarUpdater.update(player, progress); 
                plugin.checkCompletion(player, progress);
            }
        }
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;

        Player player = event.getEntity().getKiller();
        QuestProgress progress = plugin.getActiveQuests().get(player.getUniqueId());
        if (progress == null) return;

        String questType = progress.getQuest().getType();
        String questTarget = progress.getQuest().getTarget();
        String entityType = event.getEntity().getType().toString();
        
        if ("Kill".equals(questType)) {
            if (questTarget == null || questTarget.equals("ANY") || entityType.equals(questTarget)) {
                progress.increment();
                ActionBarUpdater.update(player, progress); 
                plugin.checkCompletion(player, progress);
            }
        }
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        QuestProgress progress = plugin.getActiveQuests().get(player.getUniqueId());
        if (progress == null) return;

        String questType = progress.getQuest().getType();
        String questTarget = progress.getQuest().getTarget();
        String itemType = event.getItem().getItemStack().getType().toString();
        
        if ("Collect".equals(questType)) {
            if (questTarget == null || questTarget.equals("ANY") || itemType.equals(questTarget)) {
                progress.increment();
                ActionBarUpdater.update(player, progress);  
                plugin.checkCompletion(player, progress);
            }
        }
    }

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

        player.sendMessage("§c❌ Quest failed! You died.");
    }
}