package com.example.questai.ui;

import com.example.questai.model.Quest;
import com.example.questai.model.QuestProgress;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MessageFormat {
    
    private static final String SEPARATOR = "══════════════════════════════";
    
    // Сообщение об активном квесте
    public static Component activeQuestMessage(QuestProgress progress) {
        Quest quest = progress.getQuest();
        return Component.text()
            .append(Component.text(SEPARATOR).color(NamedTextColor.GOLD))
            .appendNewline()
            .append(Component.text("⚠ У вас уже есть активный квест!").color(NamedTextColor.YELLOW))
            .appendNewline()
            .append(Component.text("").color(NamedTextColor.GRAY))
            .appendNewline()
            .append(Component.text("Текущий квест: " + quest.getType() + 
                   " (" + progress.getCurrent() + "/" + quest.getAmount() + ")").color(NamedTextColor.WHITE))
            .appendNewline()
            .append(Component.text("").color(NamedTextColor.GRAY))
            .appendNewline()
            .append(Component.text("Чтобы отменить и взять новый: §e/quest cancel").color(NamedTextColor.YELLOW))
            .appendNewline()
            .append(Component.text(SEPARATOR).color(NamedTextColor.GOLD))
            .build();
    }
    
    // Сообщение о новом квесте
    public static Component newQuestMessage(Quest quest) {
        return Component.text()
            .append(Component.text(SEPARATOR).color(NamedTextColor.GOLD))
            .appendNewline()
            .append(Component.text("✦ НОВЫЙ КВЕСТ ✦").color(NamedTextColor.GOLD))
            .appendNewline()
            .append(Component.text("").color(NamedTextColor.GRAY))
            .appendNewline()
            .append(Component.text("Тип: " + quest.getType()).color(NamedTextColor.WHITE))
            .appendNewline()
            .append(Component.text("Цель: " + formatTarget(quest)).color(NamedTextColor.WHITE))
            .appendNewline()
            .append(Component.text("Количество: " + quest.getAmount()).color(NamedTextColor.WHITE))
            .appendNewline()
            .append(Component.text("Награда: " + quest.getReward() + " XP").color(NamedTextColor.GREEN))
            .appendNewline()
            .append(Component.text("").color(NamedTextColor.GRAY))
            .appendNewline()
            .append(Component.text("Прогресс отображается над панелью инвентаря").color(NamedTextColor.YELLOW))
            .appendNewline()
            .append(Component.text("Чтобы отменить квест: §e/quest cancel").color(NamedTextColor.YELLOW))
            .appendNewline()
            .append(Component.text(SEPARATOR).color(NamedTextColor.GOLD))
            .build();
    }
    
    // Сообщение об отмене квеста
    public static Component cancelQuestMessage(Quest quest) {
        return Component.text()
            .append(Component.text(SEPARATOR).color(NamedTextColor.GOLD))
            .appendNewline()
            .append(Component.text("✖ КВЕСТ ОТМЕНЁН ✖").color(NamedTextColor.RED))
            .appendNewline()
            .append(Component.text("Вы отменили квест: " + quest.getType()).color(NamedTextColor.WHITE))
            .appendNewline()
            .append(Component.text("Используйте §e/quest §fдля получения нового").color(NamedTextColor.YELLOW))
            .appendNewline()
            .append(Component.text(SEPARATOR).color(NamedTextColor.GOLD))
            .build();
    }
    
    // Сообщение об ошибке
    public static Component errorMessage(String text) {
        return Component.text(text).color(NamedTextColor.RED);
    }
    
    // Сообщение "нет активного квеста"
    public static Component noActiveQuestMessage() {
        return Component.text("У вас нет активного квеста для отмены!").color(NamedTextColor.RED);
    }
    
    // Форматирование цели
    private static String formatTarget(Quest quest) {
        String target = quest.getTarget();
        String type = quest.getType();
        
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
    
    // Короткая цель для action bar
    public static String getShortTarget(Quest quest) {
        String target = quest.getTarget();
        String type = quest.getType();
        
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
}