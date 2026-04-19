package com.example.questai.service;

import com.example.questai.db.DBConnector;
import com.example.questai.model.Player;
import java.sql.SQLException;

public class PlayerService {
    private final DBConnector db;
    
    public PlayerService(DBConnector db) {
        this.db = db;
    }
    
    public int getOrCreatePlayerId(org.bukkit.entity.Player bukkitPlayer) throws SQLException {
        int playerId = db.getPlayerId(bukkitPlayer.getUniqueId().toString());
        if (playerId == -1) {
            db.savePlayer(bukkitPlayer.getUniqueId().toString(), bukkitPlayer.getName());
            playerId = db.getPlayerId(bukkitPlayer.getUniqueId().toString());
        }
        return playerId;
    }
    
    public Player buildGamePlayer(int playerId, org.bukkit.entity.Player bukkitPlayer) throws SQLException {
        double successRate = db.getSuccessRate(playerId);
        int kills = bukkitPlayer.getStatistic(org.bukkit.Statistic.MOB_KILLS);
        int deaths = bukkitPlayer.getStatistic(org.bukkit.Statistic.DEATHS);
        
        Player gamePlayer = new Player();
        gamePlayer.setId(playerId);
        gamePlayer.setUuid(bukkitPlayer.getUniqueId().toString());
        gamePlayer.setName(bukkitPlayer.getName());
        gamePlayer.setKills(kills);
        gamePlayer.setDeaths(deaths);
        gamePlayer.setSuccessRate(successRate);
        gamePlayer.setLastQuestType(db.getLastQuestType(playerId));
        gamePlayer.setConsecutiveSuccesses(db.getConsecutiveSuccesses(playerId));
        gamePlayer.setFavoriteType(db.getFavoriteQuestType(playerId));
        gamePlayer.setLeastFavoriteType(db.getLeastFavoriteQuestType(playerId));
        gamePlayer.setPreferredTarget(db.getPreferredTarget(playerId));
        
        return gamePlayer;
    }
}