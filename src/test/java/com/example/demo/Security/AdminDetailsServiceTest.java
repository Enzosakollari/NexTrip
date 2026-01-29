package com.example.demo.Security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.Admin.AdminUser;
import com.example.demo.Admin.AdminUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AdminDetailsServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    @InjectMocks
    private AdminDetailsService adminDetailsService;

    @Test
    void loadUserByUsername_returnsUserDetails_whenAdminFound() {
        // Arrange
        String username = "admin";
        String password = "password";
        
        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(username);
        adminUser.setPassword(password);
        adminUser.setEnabled(true);
        
        when(adminUserRepository.findByUsername(username)).thenReturn(Optional.of(adminUser));
        
        // Act
        UserDetails userDetails = adminDetailsService.loadUserByUsername(username);
        
        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        
        boolean hasAdminAuthority = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ADMIN"));
        
        assertTrue(hasAdminAuthority, "User should have ADMIN authority");
        
        verify(adminUserRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_throwsUsernameNotFoundException_whenAdminNotFound() {
        // Arrange
        String username = "nonexistent";
        
        when(adminUserRepository.findByUsername(username)).thenReturn(Optional.empty());
        
        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> adminDetailsService.loadUserByUsername(username));
        
        assertEquals("Admin not found", exception.getMessage());
        
        verify(adminUserRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_throwsDisabledException_whenAdminDisabled() {
        // Arrange
        String username = "disabled_admin";
        
        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(username);
        adminUser.setPassword("password");
        adminUser.setEnabled(false);
        
        when(adminUserRepository.findByUsername(username)).thenReturn(Optional.of(adminUser));
        
        // Act & Assert
        DisabledException exception = assertThrows(DisabledException.class,
                () -> adminDetailsService.loadUserByUsername(username));
        
        assertEquals("Admin disabled", exception.getMessage());
        
        verify(adminUserRepository).findByUsername(username);
    }
}