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

            try {

                int questId = questService.createQuest(player);

                Quest quest = QuestGenerator.generateQuest();

                QuestProgress progress = new QuestProgress(questId, quest);

                activeQuests.put(player.getUniqueId(), progress);

                updateActionBar(player, progress);

                String msg;
                switch (quest.getType()) {
                    case "Break" -> msg = "New quest started: Break " + quest.getAmount() + " blocks!";
                    case "Kill" -> msg = "New quest started: Kill " + quest.getAmount() + " mobs!";
                    case "Collect" -> msg = "New quest started: Collect " + quest.getAmount() + " items!";
                    default -> msg = "New quest started!";
                }

                player.sendMessage(Component.text(msg).color(NamedTextColor.GREEN));

            } catch (SQLException e) {
                e.printStackTrace();
                player.sendMessage(Component.text("Error creating quest").color(NamedTextColor.RED));
            }

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

    public void checkCompletion(Player player, QuestProgress progress) {

        if (progress.getCurrent() >= progress.getQuest().getAmount()) {

            int xp = progress.getQuest().getReward();
            player.giveExp(xp);

            player.sendTitle(
                    "Quest Completed!",
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