package com.example.questai.db.repositories;

import com.example.questai.db.DatabaseConnection;
import com.example.questai.model.Player;
import com.example.questai.model.Quest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class QuestRepositoryTest {

    @Mock
    private DatabaseConnection mockDbConnection;
    
    @Mock
    private Connection mockConnection;
    
    @Mock
    private PreparedStatement mockStmt;
    
    @Mock
    private ResultSet mockRs;
    
    @Mock
    private PlayerRepository mockPlayerRepository;

    @InjectMocks
    private QuestRepository questRepository;

    @Test
    void testSaveQuest() throws SQLException {
        Quest quest = new Quest();
        quest.setType("Kill");
        quest.setTarget("ZOMBIE");
        quest.setAmount(5);
        quest.setReward(50);
        
        Player player = new Player();
        player.setId(1);
        player.setDeaths(10);
        player.setKills(20);
        player.setSuccessRate(0.5);
        
        when(mockDbConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("id")).thenReturn(100);
        
        int result = questRepository.saveQuest(1, quest, player, 0.75, true);
        
        assertEquals(100, result);
        
        verify(mockStmt, times(1)).setInt(1, 1);
        verify(mockStmt, times(1)).setString(2, "Kill");
        verify(mockStmt, times(1)).setString(3, "ZOMBIE");
        verify(mockStmt, times(1)).setInt(4, 5);
        verify(mockStmt, times(1)).setInt(5, 50);
        verify(mockStmt, times(1)).setInt(6, 10);
        verify(mockStmt, times(1)).setInt(7, 20);
        verify(mockStmt, times(1)).setDouble(8, 0.5);
        verify(mockStmt, times(1)).setDouble(9, 0.75);
        verify(mockStmt, times(1)).setBoolean(10, true);
        verify(mockStmt, times(1)).executeQuery();
    }

    @Test
    void testQuestRepositoryClassExists() {
        assertNotNull(QuestRepository.class);
    }
}