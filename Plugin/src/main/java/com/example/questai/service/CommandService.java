package com.example.questai.service;

import com.example.questai.QuestPlugin;
import com.example.questai.model.Quest;
import com.example.questai.model.QuestProgress;
import com.example.questai.ui.MessageFormat;
import com.example.questai.ui.ActionBarUpdater;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class CommandService {
    
    private final QuestPlugin plugin;
    private final QuestService questService;
    
    public CommandService(QuestPlugin plugin, QuestService questService) {
        this.plugin = plugin;
        this.questService = questService;
    }
    
    public void cancelQuest(Player player) {
        QuestProgress progress = plugin.getActiveQuests().get(player.getUniqueId());
        
        if (progress == null) {
            player.sendMessage(MessageFormat.noActiveQuestMessage());
            return;
        }
        
        try {
            questService.failQuest(player, progress.getQuestId());
            plugin.getActiveQuests().remove(player.getUniqueId());
            player.sendMessage(MessageFormat.cancelQuestMessage(progress.getQuest()));
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(MessageFormat.errorMessage("Ошибка отмены квеста"));
        }
    }
    
    public void createNewQuest(Player player) {
        // Проверка активного квеста
        QuestProgress existingProgress = plugin.getActiveQuests().get(player.getUniqueId());
        if (existingProgress != null) {
            player.sendMessage(MessageFormat.activeQuestMessage(existingProgress));
            return;
        }
        
        try {
            int questId = questService.createQuest(player);
            Quest quest = plugin.getQuestById(questId);
            
            if (quest == null) {
                player.sendMessage(MessageFormat.errorMessage("Ошибка загрузки квеста"));
                return;
            }
            
            QuestProgress progress = new QuestProgress(questId, quest);
            plugin.getActiveQuests().put(player.getUniqueId(), progress);
            
            player.sendMessage(MessageFormat.newQuestMessage(quest));
            ActionBarUpdater.update(player, progress);
            
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(MessageFormat.errorMessage("Ошибка создания квеста"));
        }
    }
}