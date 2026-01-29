package com.example.demo.Booking;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.Business.TravelPackage;
import com.example.demo.Business.TravelPackageRepository;
import com.example.demo.Service.EmailService;
import com.example.demo.User.AppUser;
import com.example.demo.User.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TravelPackageRepository travelPackageRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void createBooking_throws_whenPackageNotFound() {
        Long packId = 10L;
        when(travelPackageRepository.findById(packId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> bookingService.createBooking(packId, "John", "john@mail.com", 2));

        // ndrysho mesazhin sipas kodit tënd real
        assertEquals("Package not found", ex.getMessage());

        verify(travelPackageRepository).findById(packId);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void createBooking_savesBooking_whenValid() {
        Long packId = 10L;

        TravelPackage tp = new TravelPackage();
        tp.setId(packId);
        tp.setCapacity(5);

        when(travelPackageRepository.findById(packId)).thenReturn(Optional.of(tp));

        Booking saved = new Booking();
        saved.setId(1L);

        when(bookingRepository.save(any(Booking.class))).thenReturn(saved);

        Booking result = bookingService.createBooking(packId, "John", "john@mail.com", 2);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(travelPackageRepository).findById(packId);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void getUserBookings_returnsEmpty_whenNoPrincipal() {
        List<Booking> result = bookingService.getUserBookings(null);

        assertEquals(Collections.emptyList(), result);
        verifyNoInteractions(appUserRepository);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void getUserBookings_returnsUserBookings_whenUserExists() {
        Principal principal = () -> "john";

        AppUser user = new AppUser();
        user.setId(7L);
        user.setEmail("john@example.com");

        List<Booking> bookings = List.of(new Booking());

        when(appUserRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(bookingRepository.findByEmailOrderByCreatedAtDesc("john@example.com")).thenReturn(bookings);

        List<Booking> result = bookingService.getUserBookings(principal);

        assertEquals(bookings, result);
        verify(appUserRepository).findByUsername("john");
        verify(bookingRepository).findByEmailOrderByCreatedAtDesc("john@example.com");
    }

    @Test
    void getUserBookings_marksCompleted_whenTripEnded() {
        Principal principal = () -> "john";

        AppUser user = new AppUser();
        user.setId(1L);
        user.setEmail("john@example.com");

        TravelPackage pack = new TravelPackage();
        pack.setEndDate(LocalDate.now().minusDays(1));

        Booking booking = new Booking();
        booking.setTravelPackage(pack);
        booking.setStatus(BookingStatus.CONFIRMED);

        when(appUserRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(bookingRepository.findByEmailOrderByCreatedAtDesc("john@example.com")).thenReturn(List.of(booking));

        List<Booking> result = bookingService.getUserBookings(principal);

        assertEquals(BookingStatus.COMPLETED, result.get(0).getStatus());
    }
}
