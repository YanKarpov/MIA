package com.example.questai.db.repositories;

import com.example.questai.db.DatabaseConnection;
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
class PlayerRepositoryTest {

    @Mock
    private DatabaseConnection mockDbConnection;
    
    @Mock
    private Connection mockConnection;
    
    @Mock
    private PreparedStatement mockStmt;
    
    @Mock
    private ResultSet mockRs;

    @InjectMocks
    private PlayerRepository playerRepository;

    @Test
    void testGetPlayerId() throws SQLException {
        when(mockDbConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("id")).thenReturn(123);
        
        int result = playerRepository.getPlayerId("test-uuid");
        
        assertEquals(123, result);
    }

    @Test
    void testGetPlayerIdNotFound() throws SQLException {
        when(mockDbConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);
        
        int result = playerRepository.getPlayerId("nonexistent");
        
        assertEquals(-1, result);
    }

    @Test
    void testPlayerRepositoryClassExists() {
        assertNotNull(PlayerRepository.class);
    }
}