package com.example.questai;

import com.example.questai.db.DBConnector;
import com.example.questai.generator.QuestGenerator;
import com.example.questai.listener.QuestListener;
import com.example.questai.model.Quest;
import com.example.questai.model.QuestProgress;
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

    public Map<UUID, QuestProgress> getActiveQuests() {
        return activeQuests;
    }

    public DBConnector getDb() {
        return db;
    }

    @Override
    public void onEnable() {
        getLogger().info("QuestPlugin enabled");

        // Инициализация подключения к БД
        try {
            db = new DBConnector();
        } catch (SQLException e) {
            e.printStackTrace();
            getLogger().severe("Failed to connect to database!");
        }

        // Регистрируем слушатель событий
        getServer().getPluginManager().registerEvents(new QuestListener(this), this);

        // Таск для постоянного обновления ActionBar
        new BukkitRunnable() {
            @Override
            public void run() {
                for (var entry : activeQuests.entrySet()) {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player != null && player.isOnline()) {
                        updateActionBar(player, entry.getValue());
                        // Проверяем выполнение квеста
                        checkCompletion(player, entry.getValue());
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L); // каждая секунда
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
            Quest quest = QuestGenerator.generateQuest();
            QuestProgress progress = new QuestProgress(quest);

            activeQuests.put(player.getUniqueId(), progress);
            updateActionBar(player, progress);

            // Сохраняем игрока в БД
            if (db != null) {
                try {
                    db.savePlayer(player.getUniqueId().toString(), player.getName());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            // Выводим текст в зависимости от типа квеста
            String msg;
            switch (quest.getType()) {
                case "Break" -> msg = "New quest started: Break " + quest.getAmount() + " blocks!";
                case "Kill" -> msg = "New quest started: Kill " + quest.getAmount() + " mobs!";
                case "Collect" -> msg = "New quest started: Collect " + quest.getAmount() + " items!";
                default -> msg = "New quest started!";
            }

            player.sendMessage(Component.text(msg).color(NamedTextColor.GREEN));
            return true;
        }

        return false;
    }

    public void updateActionBar(Player player, QuestProgress progress) {
        String progressText;
        switch (progress.getQuest().getType()) {
            case "Break" -> progressText = progress.getCurrent() + "/" + progress.getQuest().getAmount() + " blocks broken";
            case "Kill" -> progressText = progress.getCurrent() + "/" + progress.getQuest().getAmount() + " mobs killed";
            case "Collect" -> progressText = progress.getCurrent() + "/" + progress.getQuest().getAmount() + " items collected";
            default -> progressText = progress.getCurrent() + "/" + progress.getQuest().getAmount();
        }

        player.sendActionBar(Component.text(
                "Quest [" + progress.getQuest().getType() + "]: " + progressText
        ).color(NamedTextColor.GREEN));
    }

    // Проверка выполнения квеста и выдача XP с сохранением квеста в БД
    public void checkCompletion(Player player, QuestProgress progress) {
        if (progress.getCurrent() >= progress.getQuest().getAmount()) {
            int xp = parseRewardXp(progress.getQuest().getReward());
            player.giveExp(xp);

            player.sendTitle(
                    "Quest Completed!",
                    "Reward: " + xp + " XP",
                    10, 70, 20
            );

            activeQuests.remove(player.getUniqueId());

            // Сохраняем квест в БД
            if (db != null) {
                try {
                    int playerId = db.getPlayerId(player.getUniqueId().toString());
                    if (playerId != -1) {
                        db.saveQuest(
                                playerId,
                                progress.getQuest().getType(),
                                progress.getQuest().getTarget(),
                                progress.getQuest().getAmount(),
                                progress.getQuest().getReward(),
                                true
                        );
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int parseRewardXp(String reward) {
        try {
            return Integer.parseInt(reward.split(" ")[0]);
        } catch (Exception e) {
            return 0;
        }
    }
}