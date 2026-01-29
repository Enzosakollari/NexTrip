package com.example.demo.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.Business.TravelPackage;
import com.example.demo.Business.TravelPackageRepository;
import com.example.demo.User.BusinessUser;
import com.example.demo.User.BusinessUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class BusinessUserServiceTest {

    @Mock
    private BusinessUserRepository businessRepo;

    @Mock
    private TravelPackageRepository packRepo;

    @InjectMocks
    private BusinessUserService businessUserService;

    @Test
    void loadUserByUsername_returnsUserDetails_whenBusinessFound() {
        // Arrange
        String username = "business1";
        String password = "password";

        BusinessUser businessUser = new BusinessUser();
        businessUser.setUsername(username);
        businessUser.setPassword(password);

        when(businessRepo.findByUsername(username)).thenReturn(Optional.of(businessUser));

        // Act
        UserDetails userDetails = businessUserService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty());

        verify(businessRepo).findByUsername(username);
    }

    @Test
    void loadUserByUsername_throwsException_whenBusinessNotFound() {
        // Arrange
        String username = "nonexistent";

        when(businessRepo.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> businessUserService.loadUserByUsername(username));

        assertEquals("Business not found: " + username, exception.getMessage());

        verify(businessRepo).findByUsername(username);
    }

    @Test
    void currentBusiness_returnsNull_whenPrincipalIsNull() {
        // Act
        BusinessUser result = businessUserService.currentBusiness(null);

        // Assert
        assertNull(result);

        verifyNoInteractions(businessRepo);
    }

    @Test
    void currentBusiness_returnsBusiness_whenFound() {
        // Arrange
        String username = "business1";

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(username);

        BusinessUser businessUser = new BusinessUser();
        businessUser.setUsername(username);

        when(businessRepo.findByUsername(username)).thenReturn(Optional.of(businessUser));

        // Act
        BusinessUser result = businessUserService.currentBusiness(principal);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());

        verify(businessRepo).findByUsername(username);
    }

    @Test
    void currentBusiness_returnsNull_whenNotFound() {
        // Arrange
        String username = "nonexistent";

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(username);

        when(businessRepo.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        BusinessUser result = businessUserService.currentBusiness(principal);

        // Assert
        assertNull(result);

        verify(businessRepo).findByUsername(username);
    }

    @Test
    void getMyPackages_returnsPackages_fromRepository() {
        // Arrange
        Long businessId = 1L;
        List<TravelPackage> packages = List.of(new TravelPackage(), new TravelPackage());

        when(packRepo.findByBusinessUserId(businessId)).thenReturn(packages);

        // Act
        List<TravelPackage> result = businessUserService.getMyPackages(businessId);

        // Assert
        assertEquals(packages, result);

        verify(packRepo).findByBusinessUserId(businessId);
    }

    @Test
    void getPackageForBusiness_returnsPackage_whenFound() {
        // Arrange
        Long businessId = 1L;
        Long packId = 10L;

        BusinessUser businessUser = new BusinessUser();
        businessUser.setId(businessId);

        TravelPackage travelPackage = new TravelPackage();
        travelPackage.setId(packId);
        travelPackage.setBusinessUser(businessUser);

        when(packRepo.findById(packId)).thenReturn(Optional.of(travelPackage));

        // Act
        TravelPackage result = businessUserService.getPackageForBusiness(businessId, packId);

        // Assert
        assertNotNull(result);
        assertEquals(packId, result.getId());

        verify(packRepo).findById(packId);
    }

    @Test
    void getPackageForBusiness_throwsException_whenPackageNotFound() {
        // Arrange
        Long businessId = 1L;
        Long packId = 10L;

        when(packRepo.findById(packId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> businessUserService.getPackageForBusiness(businessId, packId));

        assertEquals("Pack not found", exception.getMessage());

        verify(packRepo).findById(packId);
    }

    @Test
    void getPackageForBusiness_throwsException_whenPackageNotOwnedByBusiness() {
        // Arrange
        Long businessId = 1L;
        Long otherBusinessId = 2L;
        Long packId = 10L;

        BusinessUser otherBusinessUser = new BusinessUser();
        otherBusinessUser.setId(otherBusinessId);

        TravelPackage travelPackage = new TravelPackage();
        travelPackage.setId(packId);
        travelPackage.setBusinessUser(otherBusinessUser);

        when(packRepo.findById(packId)).thenReturn(Optional.of(travelPackage));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> businessUserService.getPackageForBusiness(businessId, packId));

        assertEquals("Not allowed", exception.getMessage());

        verify(packRepo).findById(packId);
    }

    @Test
    void createPackage_savesPackage_withBusinessUser() {
        // Arrange
        Long businessId = 1L;

        BusinessUser businessUser = new BusinessUser();
        businessUser.setId(businessId);

        TravelPackage travelPackage = new TravelPackage();
        travelPackage.setTitle("Test Package");

        when(businessRepo.findById(businessId)).thenReturn(Optional.of(businessUser));

        // Act
        businessUserService.createPackage(businessId, travelPackage);

        // Assert
        assertNull(travelPackage.getId());
        assertEquals(businessUser, travelPackage.getBusinessUser());

        verify(businessRepo).findById(businessId);
        verify(packRepo).save(travelPackage);
    }

    @Test
    void createPackage_throwsException_whenBusinessNotFound() {
        // Arrange
        Long businessId = 1L;

        TravelPackage travelPackage = new TravelPackage();

        when(businessRepo.findById(businessId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> businessUserService.createPackage(businessId, travelPackage));

        assertEquals("Business not found", exception.getMessage());

        verify(businessRepo).findById(businessId);
        verifyNoInteractions(packRepo);
    }

    @Test
    void updatePackage_updatesPackage_whenFound() {
        // Arrange
        Long businessId = 1L;
        Long packId = 10L;

        BusinessUser businessUser = new BusinessUser();
        businessUser.setId(businessId);

        TravelPackage existingPackage = new TravelPackage();
        existingPackage.setId(packId);
        existingPackage.setBusinessUser(businessUser);
        existingPackage.setTitle("Old Title");

        TravelPackage updatedPackage = new TravelPackage();
        updatedPackage.setTitle("New Title");
        updatedPackage.setDestination("New Destination");
        updatedPackage.setDurationDays(7);
        updatedPackage.setDescription("New Description");
        updatedPackage.setPrice(new BigDecimal("100.0"));
        updatedPackage.setCurrency("USD");

        when(packRepo.findById(packId)).thenReturn(Optional.of(existingPackage));

        // Act
        businessUserService.updatePackage(businessId, packId, updatedPackage);

        // Assert
        assertEquals("New Title", existingPackage.getTitle());
        assertEquals("New Destination", existingPackage.getDestination());
        assertEquals(7, existingPackage.getDurationDays());
        assertEquals("New Description", existingPackage.getDescription());
        assertEquals(new BigDecimal("100.0"), existingPackage.getPrice());
        assertEquals("USD", existingPackage.getCurrency());

        verify(packRepo).findById(packId);
        verify(packRepo).save(existingPackage);
    }

    @Test
    void deletePackage_deletesPackage_whenFound() {
        // Arrange
        Long businessId = 1L;
        Long packId = 10L;

        BusinessUser businessUser = new BusinessUser();
        businessUser.setId(businessId);

        TravelPackage travelPackage = new TravelPackage();
        travelPackage.setId(packId);
        travelPackage.setBusinessUser(businessUser);

        when(packRepo.findById(packId)).thenReturn(Optional.of(travelPackage));

        // Act
        businessUserService.deletePackage(businessId, packId);

        // Assert
        verify(packRepo).findById(packId);
        verify(packRepo).delete(travelPackage);
    }
}
