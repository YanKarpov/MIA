package com.example.questai;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class QuestAIPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("QuestAIPlugin enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("QuestAIPlugin disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            return true;
        }

        if (command.getName().equalsIgnoreCase("quest")) {

            Quest quest = QuestGenerator.generateQuest();

            player.sendMessage(Component.text(
                    "New quest: " +
                    quest.getType() + " " +
                    quest.getAmount() + " " +
                    quest.getTarget()
            ).color(NamedTextColor.GREEN));

            return true;
        }

        return false;
    }
}