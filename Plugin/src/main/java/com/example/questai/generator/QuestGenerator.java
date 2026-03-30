package com.example.questai.generator;

import com.example.questai.model.Quest;
import java.util.Random;

public class QuestGenerator {

    private static final Random rand = new Random();
    private static final String[] TYPES = {"Break", "Kill", "Collect"};

    public static Quest generateQuest() {
        Quest q = new Quest();

        String type = TYPES[rand.nextInt(TYPES.length)];
        q.setType(type);

        switch (type) {
            case "Break":
                q.setTarget("ANY");
                q.setAmount(rand.nextInt(3) + 1);
                q.setReward(5); 
                break;

            case "Kill":
                q.setTarget("ANY");
                q.setAmount(rand.nextInt(3) + 1);
                q.setReward(10);
                break;

            case "Collect":
                q.setTarget("ANY");
                q.setAmount(rand.nextInt(2) + 1);
                q.setReward(15);
                break;
        }

        return q;
    }
}