package com.example.questai;

import com.example.questai.model.Quest;
import com.example.questai.model.QuestProgress;
import com.example.questai.generator.QuestGenerator;
import com.example.questai.listener.QuestListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestPlugin extends JavaPlugin {

    private final Map<UUID, QuestProgress> activeQuests = new HashMap<>();

    public Map<UUID, QuestProgress> getActiveQuests() {
        return activeQuests;
    }

    @Override
    public void onEnable() {
        getLogger().info("QuestPlugin enabled");

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
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L); // каждая секунда
    }

    @Override
    public void onDisable() {
        getLogger().info("QuestPlugin disabled");
    }

    // Команда /quest
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

            player.sendMessage(Component.text(
                    "New quest started: Break " +
                    quest.getAmount() + " blocks!"
            ).color(NamedTextColor.GREEN));

            return true;
        }

        return false;
    }

    // Обновление ActionBar
    public void updateActionBar(Player player, QuestProgress progress) {
        player.sendActionBar(Component.text(
                "Quest: " +
                progress.getQuest().getType() + " " +
                progress.getCurrent() + "/" +
                progress.getQuest().getAmount() + " blocks broken"
        ).color(NamedTextColor.GREEN));
    }

    // Проверка выполнения квеста и выдача XP
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
        }
    }

    // Парсер награды в виде XP
    private int parseRewardXp(String reward) {
        try {
            return Integer.parseInt(reward.split(" ")[0]);
        } catch (Exception e) {
            return 0;
        }
    }
}