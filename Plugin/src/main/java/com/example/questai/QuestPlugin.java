package com.example.questai;

import com.example.questai.generator.QuestGenerator;
import com.example.questai.db.DBConnector;
import com.example.questai.listener.QuestListener;
import com.example.questai.model.Quest;
import com.example.questai.model.QuestProgress;
import com.example.questai.service.QuestService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
                        updateActionBar(player, entry.getValue());
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
            
            // Проверяем аргументы для отмены квеста
            if (args.length > 0 && args[0].equalsIgnoreCase("cancel")) {
                cancelQuest(player);
                return true;
            }
            
            // Если есть активный квест, предлагаем его отменить или заменяем
            QuestProgress existingProgress = activeQuests.get(player.getUniqueId());
            if (existingProgress != null) {
                player.sendMessage(Component.text("══════════════════════════════").color(NamedTextColor.GOLD));
                player.sendMessage(Component.text("⚠ У вас уже есть активный квест!").color(NamedTextColor.YELLOW));
                player.sendMessage(Component.text("").color(NamedTextColor.GRAY));
                player.sendMessage(Component.text("Текущий квест: " + existingProgress.getQuest().getType() + 
                                   " (" + existingProgress.getCurrent() + "/" + 
                                   existingProgress.getQuest().getAmount() + ")").color(NamedTextColor.WHITE));
                player.sendMessage(Component.text("").color(NamedTextColor.GRAY));
                player.sendMessage(Component.text("Чтобы отменить и взять новый: §e/quest cancel").color(NamedTextColor.YELLOW));
                player.sendMessage(Component.text("══════════════════════════════").color(NamedTextColor.GOLD));
                return true;
            }

            try {
                int questId = questService.createQuest(player);
                
                Quest quest = getQuestFromDB(questId);
                if (quest == null) {
                    player.sendMessage(Component.text("Error loading quest").color(NamedTextColor.RED));
                    return true;
                }

                QuestProgress progress = new QuestProgress(questId, quest);
                activeQuests.put(player.getUniqueId(), progress);

                // Отображаем новый квест
                player.sendMessage(Component.text("══════════════════════════════").color(NamedTextColor.GOLD));
                player.sendMessage(Component.text("✦ НОВЫЙ КВЕСТ ✦").color(NamedTextColor.GOLD));
                player.sendMessage(Component.text("").color(NamedTextColor.GRAY));
                
                String targetText = formatTarget(quest.getTarget(), quest.getType());
                player.sendMessage(Component.text("Тип: " + quest.getType()).color(NamedTextColor.WHITE));
                player.sendMessage(Component.text("Цель: " + targetText).color(NamedTextColor.WHITE));
                player.sendMessage(Component.text("Количество: " + quest.getAmount()).color(NamedTextColor.WHITE));
                player.sendMessage(Component.text("Награда: " + quest.getReward() + " XP").color(NamedTextColor.GREEN));
                player.sendMessage(Component.text("").color(NamedTextColor.GRAY));
                player.sendMessage(Component.text("Прогресс отображается над панелью инвентаря").color(NamedTextColor.YELLOW));
                player.sendMessage(Component.text("Чтобы отменить квест: §e/quest cancel").color(NamedTextColor.YELLOW));
                player.sendMessage(Component.text("══════════════════════════════").color(NamedTextColor.GOLD));

                updateActionBar(player, progress);

            } catch (SQLException e) {
                e.printStackTrace();
                player.sendMessage(Component.text("Error creating quest").color(NamedTextColor.RED));
            }

            return true;
        }

        return false;
    }
    
    private void cancelQuest(Player player) {
        QuestProgress progress = activeQuests.get(player.getUniqueId());
        if (progress == null) {
            player.sendMessage(Component.text("У вас нет активного квеста для отмены!").color(NamedTextColor.RED));
            return;
        }
        
        try {
            // Отменяем квест в БД (помечаем как FAILED)
            questService.failQuest(player, progress.getQuestId());
            
            // Удаляем из активных квестов
            activeQuests.remove(player.getUniqueId());
            
            player.sendMessage(Component.text("══════════════════════════════").color(NamedTextColor.GOLD));
            player.sendMessage(Component.text("✖ КВЕСТ ОТМЕНЁН ✖").color(NamedTextColor.RED));
            player.sendMessage(Component.text("Вы отменили квест: " + progress.getQuest().getType()).color(NamedTextColor.WHITE));
            player.sendMessage(Component.text("Используйте §e/quest §fдля получения нового").color(NamedTextColor.YELLOW));
            player.sendMessage(Component.text("══════════════════════════════").color(NamedTextColor.GOLD));
            
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(Component.text("Error cancelling quest").color(NamedTextColor.RED));
        }
    }
    
    private Quest getQuestFromDB(int questId) throws SQLException {
        return db.getQuestById(questId);
    }
    
    private String formatTarget(String target, String type) {
        if (target == null || target.equals("ANY")) {
            switch (type) {
                case "Break": return "блоков";
                case "Kill": return "мобов";
                case "Collect": return "предметов";
                default: return "цель";
            }
        }
        
        switch (type) {
            case "Break": return "сломать " + target.toLowerCase();
            case "Kill": return "убить " + target.toLowerCase();
            case "Collect": return "собрать " + target.toLowerCase();
            default: return target;
        }
    }

    public void updateActionBar(Player player, QuestProgress progress) {
        Quest quest = progress.getQuest();
        String targetShort = getShortTarget(quest.getTarget(), quest.getType());
        
        String progressText = progress.getCurrent() + "/" + quest.getAmount() + " " + targetShort;
        player.sendActionBar(Component.text(
                "📋 " + quest.getType() + ": " + progressText
        ).color(NamedTextColor.GREEN));
    }
    
    private String getShortTarget(String target, String type) {
        if (target == null || target.equals("ANY")) {
            switch (type) {
                case "Break": return "блоков";
                case "Kill": return "мобов";
                case "Collect": return "предметов";
                default: return "целей";
            }
        }
        return target.toLowerCase();
    }

    public void checkCompletion(Player player, QuestProgress progress) {
        if (progress.getCurrent() >= progress.getQuest().getAmount()) {
            int xp = progress.getQuest().getReward();
            player.giveExp(xp);

            player.sendTitle(
                    "✅ Quest Completed!",
                    "Reward: " + xp + " XP",
                    10, 70, 20
            );

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