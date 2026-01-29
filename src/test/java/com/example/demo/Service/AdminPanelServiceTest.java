package com.example.demo.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.Booking.Booking;
import com.example.demo.Booking.BookingRepository;
import com.example.demo.Business.TravelPackage;
import com.example.demo.Business.TravelPackageRepository;
import com.example.demo.User.AppUser;
import com.example.demo.User.AppUserRepository;
import com.example.demo.User.BusinessUser;
import com.example.demo.User.BusinessUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class AdminPanelServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private BusinessUserRepository businessUserRepository;

    @Mock
    private TravelPackageRepository travelPackageRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private AdminPanelService adminPanelService;

    @Test
    void usersCount_returnsCount_fromRepository() {
        // Arrange
        when(appUserRepository.count()).thenReturn(5L);

        // Act
        long count = adminPanelService.usersCount();

        // Assert
        assertEquals(5L, count);
        verify(appUserRepository).count();
    }

    @Test
    void businessesCount_returnsCount_fromRepository() {
        // Arrange
        when(businessUserRepository.count()).thenReturn(3L);

        // Act
        long count = adminPanelService.businessesCount();

        // Assert
        assertEquals(3L, count);
        verify(businessUserRepository).count();
    }

    @Test
    void packsCount_returnsCount_fromRepository() {
        // Arrange
        when(travelPackageRepository.count()).thenReturn(10L);

        // Act
        long count = adminPanelService.packsCount();

        // Assert
        assertEquals(10L, count);
        verify(travelPackageRepository).count();
    }

    @Test
    void bookingsCount_returnsCount_fromRepository() {
        // Arrange
        when(bookingRepository.count()).thenReturn(20L);

        // Act
        long count = adminPanelService.bookingsCount();

        // Assert
        assertEquals(20L, count);
        verify(bookingRepository).count();
    }

    @Test
    void allUsers_returnsUsers_fromRepository() {
        // Arrange
        List<AppUser> users = List.of(new AppUser(), new AppUser());
        when(appUserRepository.findAll(any(Sort.class))).thenReturn(users);

        // Act
        List<AppUser> result = adminPanelService.allUsers();

        // Assert
        assertEquals(users, result);
        verify(appUserRepository).findAll(any(Sort.class));
    }

    @Test
    void allBusinesses_returnsBusinesses_fromRepository() {
        // Arrange
        List<BusinessUser> businesses = List.of(new BusinessUser(), new BusinessUser());
        when(businessUserRepository.findAll(any(Sort.class))).thenReturn(businesses);

        // Act
        List<BusinessUser> result = adminPanelService.allBusinesses();

        // Assert
        assertEquals(businesses, result);
        verify(businessUserRepository).findAll(any(Sort.class));
    }

    @Test
    void allPacks_returnsPacks_fromRepository() {
        // Arrange
        List<TravelPackage> packs = List.of(new TravelPackage(), new TravelPackage());
        when(travelPackageRepository.findAll(any(Sort.class))).thenReturn(packs);

        // Act
        List<TravelPackage> result = adminPanelService.allPacks();

        // Assert
        assertEquals(packs, result);
        verify(travelPackageRepository).findAll(any(Sort.class));
    }

    @Test
    void allBookings_returnsBookings_fromRepository() {
        // Arrange
        List<Booking> bookings = List.of(new Booking(), new Booking());
        when(bookingRepository.findAll(any(Sort.class))).thenReturn(bookings);

        // Act
        List<Booking> result = adminPanelService.allBookings();

        // Assert
        assertEquals(bookings, result);
        verify(bookingRepository).findAll(any(Sort.class));
    }

    @Test
    void allBookings_fallsBackToSortById_whenSortByCreatedAtFails() {
        // Arrange
        List<Booking> bookings = List.of(new Booking(), new Booking());
        
        // First call throws exception, second call returns bookings
        when(bookingRepository.findAll(any(Sort.class)))
                .thenThrow(new RuntimeException("Sort by createdAt failed"))
                .thenReturn(bookings);

        // Act
        List<Booking> result = adminPanelService.allBookings();

        // Assert
        assertEquals(bookings, result);
        verify(bookingRepository, times(2)).findAll(any(Sort.class));
    }

    @Test
    void deleteUser_callsRepository() {
        // Arrange
        Long userId = 1L;

        // Act
        adminPanelService.deleteUser(userId);

        // Assert
        verify(appUserRepository).deleteById(userId);
    }

    @Test
    void deleteBusiness_callsRepository() {
        // Arrange
        Long businessId = 1L;

        // Act
        adminPanelService.deleteBusiness(businessId);

        // Assert
        verify(businessUserRepository).deleteById(businessId);
    }

    @Test
    void deletePack_callsRepository() {
        // Arrange
        Long packId = 1L;

        // Act
        adminPanelService.deletePack(packId);

        // Assert
        verify(travelPackageRepository).deleteById(packId);
    }

    @Test
    void deleteBooking_callsRepository() {
        // Arrange
        Long bookingId = 1L;

        // Act
        adminPanelService.deleteBooking(bookingId);

        // Assert
        verify(bookingRepository).deleteById(bookingId);
    }
}