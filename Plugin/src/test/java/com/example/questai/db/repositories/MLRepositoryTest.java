package com.example.questai.db.repositories;

import com.example.questai.db.DatabaseConnection;
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
class MLRepositoryTest {

    @Mock
    private DatabaseConnection mockDbConnection;
    
    @Mock
    private Connection mockConnection;
    
    @Mock
    private PreparedStatement mockStmt;
    
    @Mock
    private ResultSet mockRs;

    @InjectMocks
    private MLRepository mlRepository;

    @Test
    void testSaveMlPrediction() throws SQLException {
        Quest candidate = new Quest();
        candidate.setType("Kill");
        candidate.setTarget("SPIDER");
        candidate.setAmount(3);
        candidate.setReward(60);
        
        when(mockDbConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1);
        
        mlRepository.saveMlPrediction(1, 0, 0.85, true, candidate);
        
        verify(mockStmt, times(1)).executeUpdate();
    }

    @Test
    void testGetLatestPredictions() throws SQLException {
        when(mockDbConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);
        
        var result = mlRepository.getLatestPredictions(5);
        
        assertNotNull(result);
        verify(mockStmt, times(1)).executeQuery();
    }

    @Test
    void testMLRepositoryClassExists() {
        assertNotNull(MLRepository.class);
    }
}