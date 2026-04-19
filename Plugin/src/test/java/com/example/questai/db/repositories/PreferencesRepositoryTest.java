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
class PreferencesRepositoryTest {

    @Mock
    private DatabaseConnection mockDbConnection;
    
    @Mock
    private Connection mockConnection;
    
    @Mock
    private PreparedStatement mockStmt;
    
    @Mock
    private ResultSet mockRs;

    @InjectMocks
    private PreferencesRepository preferencesRepository;

    @Test
    void testGetLastQuestType() throws SQLException {
        when(mockDbConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getString("type")).thenReturn("Kill");
        
        String result = preferencesRepository.getLastQuestType(1);
        
        assertEquals("Kill", result);
    }

    @Test
    void testPreferencesRepositoryClassExists() {
        assertNotNull(PreferencesRepository.class);
    }
}