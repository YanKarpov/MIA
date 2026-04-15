package com.example.questai;

import com.example.questai.db.DBConnector;
import com.example.questai.listener.QuestListener;
import com.example.questai.model.Quest;
import com.example.questai.model.QuestProgress;
import com.example.questai.service.QuestService;
import com.example.questai.ui.MessageFormat;
import com.example.questai.ui.ActionBarUpdater;
import com.example.questai.ui.TitleSender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestPlugin extends JavaPlugin {

    private final Map<UUID, QuestProgress> activeQuests = new HashMap<>();
    private DBConnector db;
    private QuestService questService;

    @Override
    public void onEnable() {
        getLogger().info("QuestPlugin enabled");

        try {
            db = new DBConnector();
            questService = new QuestService(db);
        } catch (SQLException e) {
            e.printStackTrace();
            getLogger().severe("Failed to connect to database!");
        }

        getServer().getPluginManager().registerEvents(new QuestListener(this), this);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (var entry : activeQuests.entrySet()) {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player != null && player.isOnline()) {
                        ActionBarUpdater.update(player, entry.getValue());
                        checkCompletion(player, entry.getValue());
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    @Override
    public void onDisable() {
        getLogger().info("QuestPlugin disabled");
        if (db != null) db.close();
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender,
                             org.bukkit.command.Command command,
                             String label,
                             String[] args) {

        if (!(sender instanceof Player player)) return true;

        if (command.getName().equalsIgnoreCase("quest")) {
            
            // Отмена квеста
            if (args.length > 0 && args[0].equalsIgnoreCase("cancel")) {
                cancelQuest(player);
                return true;
            }
            
            // Проверка активного квеста
            QuestProgress existingProgress = activeQuests.get(player.getUniqueId());
            if (existingProgress != null) {
                player.sendMessage(MessageFormat.activeQuestMessage(existingProgress));
                return true;
            }

            // Создание нового квеста
            try {
                int questId = questService.createQuest(player);
                Quest quest = getQuestFromDB(questId);
                
                if (quest == null) {
                    player.sendMessage(MessageFormat.errorMessage("Ошибка загрузки квеста"));
                    return true;
                }

                QuestProgress progress = new QuestProgress(questId, quest);
                activeQuests.put(player.getUniqueId(), progress);

                player.sendMessage(MessageFormat.newQuestMessage(quest));
                ActionBarUpdater.update(player, progress);

            } catch (SQLException e) {
                e.printStackTrace();
                player.sendMessage(MessageFormat.errorMessage("Ошибка создания квеста"));
            }

            return true;
        }

        return false;
    }
    
    private void cancelQuest(Player player) {
        QuestProgress progress = activeQuests.get(player.getUniqueId());
        
        if (progress == null) {
            player.sendMessage(MessageFormat.noActiveQuestMessage());
            return;
        }
        
        try {
            questService.failQuest(player, progress.getQuestId());
            activeQuests.remove(player.getUniqueId());
            player.sendMessage(MessageFormat.cancelQuestMessage(progress.getQuest()));
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(MessageFormat.errorMessage("Ошибка отмены квеста"));
        }
    }
    
    private Quest getQuestFromDB(int questId) throws SQLException {
        return db.getQuestById(questId);
    }

    public void checkCompletion(Player player, QuestProgress progress) {
        if (progress.getCurrent() >= progress.getQuest().getAmount()) {
            int xp = progress.getQuest().getReward();
            player.giveExp(xp);
            
            TitleSender.sendCompletionTitle(player, xp);
            activeQuests.remove(player.getUniqueId());

            try {
                questService.completeQuest(player, progress.getQuestId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<UUID, QuestProgress> getActiveQuests() {
        return activeQuests;
    }

    public QuestService getQuestService() {
        return questService;
    }
}