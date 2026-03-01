package com.example.questai.model;

public class Quest {
    private String type;    
    private String target;  
    private int amount;     
    private String reward;  

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public String getReward() { return reward; }
    public void setReward(String reward) { this.reward = reward; }
}