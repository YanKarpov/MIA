package com.example.questai.generator;

import com.example.questai.model.Quest;
import java.util.Random;

public class QuestGenerator {

    private static final Random rand = new Random();

    public static Quest generateQuest() {
        Quest q = new Quest();
        q.setType("Break");       // Квест на слом блоков
        q.setTarget("ANY");       // Любой блокп
        q.setAmount(rand.nextInt(3) + 1); // Рандомное количество 1-3
        q.setReward("5 XP");
        return q;
    }
}