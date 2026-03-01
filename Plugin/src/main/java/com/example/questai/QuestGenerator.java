package com.example.questai;

import java.util.Random;

public class QuestGenerator {

    private static final Random random = new Random();

    public static Quest generateQuest() {

        Quest quest = new Quest();

        int r = random.nextInt(3);

        switch (r) {
            case 0 -> {
                quest.setType("Kill");
                quest.setTarget("zombies");
                quest.setAmount(3 + random.nextInt(5));
                quest.setReward("XP");
            }
            case 1 -> {
                quest.setType("Collect");
                quest.setTarget("iron");
                quest.setAmount(5 + random.nextInt(10));
                quest.setReward("Items");
            }
            case 2 -> {
                quest.setType("Explore");
                quest.setTarget("a village");
                quest.setAmount(1);
                quest.setReward("Reward");
            }
        }

        return quest;
    }
}