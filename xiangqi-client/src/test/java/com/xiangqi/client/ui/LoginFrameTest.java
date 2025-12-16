package com.xiangqi.client.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LoginFrame
 */
public class LoginFrameTest {
    private LoginFrame loginFrame;
    
    @BeforeEach
    public void setUp() {
        loginFrame = new LoginFrame();
        
        // Set up test listener
        loginFrame.setLoginListener(new LoginFrame.LoginListener() {
            @Override
            public void onLoginAttempt(String username, String password) {
                // Test implementation - just verify no exceptions
            }
        });
    }
    
    @Test
    public void testLoginFrameInitialization() {
        assertNotNull(loginFrame);
        assertEquals("象棋游戏 - 登录", loginFrame.getTitle());
        assertFalse(loginFrame.isVisible());
    }
    
    @Test
    public void testShowLoginDialog() {
        loginFrame.showLoginDialog();
        assertTrue(loginFrame.isVisible());
        assertEquals("", loginFrame.getUsername());
        assertEquals("", loginFrame.getPassword());
    }
    
    @Test
    public void testLoginSuccess() {
        loginFrame.onLoginSuccess();
        // Should show success status
        // This is mainly testing that no exceptions are thrown
    }
    
    @Test
    public void testLoginFailure() {
        loginFrame.onLoginFailure("Invalid credentials");
        // Should show error message and clear password
        // This is mainly testing that no exceptions are thrown
        assertEquals("", loginFrame.getPassword());
    }
    
    @Test
    public void testClearFields() {
        // Set some values first
        loginFrame.showLoginDialog();
        
        // Clear fields
        loginFrame.clearFields();
        assertEquals("", loginFrame.getUsername());
        assertEquals("", loginFrame.getPassword());
    }
    
    @Test
    public void testShowError() {
        loginFrame.showError("Test error message");
        // Should display error without throwing exceptions
    }
}