package com.example.questai.ml;

import java.util.List;

public class RankRequest {

    private PlayerDTO player;
    private List<QuestDTO> candidates;

    public RankRequest(PlayerDTO player, List<QuestDTO> candidates) {
        this.player = player;
        this.candidates = candidates;
    }

    public PlayerDTO getPlayer() {
        return player;
    }

    public List<QuestDTO> getCandidates() {
        return candidates;
    }
}