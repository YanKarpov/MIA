package com.example.questai.model;

public class Quest {

    private int id;
    private String type;
    private String target;
    private int amount;
    private int reward;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public int getReward() { return reward; }   
    public void setReward(int reward) { this.reward = reward; }
}