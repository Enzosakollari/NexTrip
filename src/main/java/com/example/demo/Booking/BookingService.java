package com.example.demo.Booking;

import com.example.demo.Business.TravelPackage;
import com.example.demo.Business.TravelPackageRepository;
import com.example.demo.Service.EmailService;
import com.example.demo.User.AppUser;
import com.example.demo.User.AppUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TravelPackageRepository travelPackageRepository;
    private final AppUserRepository appUserRepository;
    private final EmailService emailService;

    private int bookedSeats(Long packId) {
        Integer total = bookingRepository.sumTravelersByPackId(packId);
        return total == null ? 0 : total;
    }

    private void ensureCapacity(TravelPackage pack, int travelersCount) {
        if (pack.getCapacity() <= 0) {
            throw new IllegalStateException("This pack is fully booked.");
        }
        int totalAfter = bookedSeats(pack.getId()) + travelersCount;
        if (totalAfter > pack.getCapacity()) {
            throw new IllegalStateException("This pack is fully booked.");
        }
    }

    public boolean isFullyBooked(TravelPackage pack) {
        if (pack.getCapacity() <= 0) {
            return true;
        }
        return bookedSeats(pack.getId()) >= pack.getCapacity();
    }

    public boolean isFullyBooked(Long packId) {
        TravelPackage pack = travelPackageRepository.findById(packId)
                .orElseThrow(() -> new IllegalArgumentException("Pack not found"));
        return isFullyBooked(pack);
    }

    @Transactional
    public void createBooking(Long packId, String fullName, String email, String phone, int travelersCount, Principal principal) {

        var pack = travelPackageRepository.findById(packId)
                .orElseThrow(() -> new IllegalArgumentException("Pack not found"));

        AppUser user = null;
        if (principal != null) {
            user = appUserRepository.findByUsername(principal.getName()).orElse(null);
        }

        ensureCapacity(pack, travelersCount);

        Booking booking = new Booking();
        booking.setTravelPackage(pack);
        booking.setAppUser(user);
        booking.setFullName(fullName);
        booking.setEmail(email);
        booking.setPhone(phone);
        booking.setTravelersCount(travelersCount);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCreatedAt(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);
        sendTicketIfPossible(saved);
    }

    @Transactional
    public void createBookingAfterPayment(
            Long packId,
            String fullName,
            String email,
            String phone,
            int travelersCount,
            String username,
            String stripeSessionId
    ) {
        if (stripeSessionId != null && bookingRepository.existsByStripeSessionId(stripeSessionId)) {
            return;
        }

        var pack = travelPackageRepository.findById(packId)
                .orElseThrow(() -> new IllegalArgumentException("Pack not found"));

        AppUser user = null;
        if (username != null && !username.isBlank()) {
            user = appUserRepository.findByUsername(username).orElse(null);
        }

        ensureCapacity(pack, travelersCount);

        Booking booking = new Booking();
        booking.setTravelPackage(pack);
        booking.setAppUser(user);
        booking.setFullName(fullName);
        booking.setEmail(email);
        booking.setPhone(phone);
        booking.setTravelersCount(travelersCount);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setStripeSessionId(stripeSessionId);

        Booking saved = bookingRepository.save(booking);
        sendTicketIfPossible(saved);
    }

    @Transactional
    public Booking createBooking(Long packId, String fullName, String email, int travelersCount) {
        var pack = travelPackageRepository.findById(packId)
                .orElseThrow(() -> new RuntimeException("Package not found"));

        ensureCapacity(pack, travelersCount);

        Booking booking = new Booking();
        booking.setTravelPackage(pack);
        booking.setFullName(fullName);
        booking.setEmail(email);
        booking.setTravelersCount(travelersCount);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCreatedAt(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);
        sendTicketIfPossible(saved);
        return saved;
    }

    @Transactional
    public List<Booking> businessBookings(Long businessId) {
        return bookingRepository.findAllByBusinessId(businessId);
    }

    @Transactional
    public void updateStatus(Long bookingId, Long businessId, BookingStatus status) {
        Booking booking = bookingRepository.findByIdAndBusinessId(bookingId, businessId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found or not yours"));

        booking.setStatus(status);
        bookingRepository.save(booking);
    }

    @Transactional
    public List<Booking> getUserBookings(Principal principal) {
        if (principal == null) {
            return Collections.emptyList();
        }
        AppUser user = appUserRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return Collections.emptyList();
        }
        List<Booking> bookings = bookingRepository.findByEmailOrderByCreatedAtDesc(user.getEmail());
        markCompletedBookings(bookings);
        return bookings;
    }

    private void sendTicketIfPossible(Booking booking) {
        if (booking == null) {
            return;
        }
        String email = booking.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        try {
            emailService.sendTicketEmail(booking);
        } catch (Exception ex) {
            System.err.println("Failed to send ticket email: " + ex.getMessage());
        }
    }

    private void markCompletedBookings(List<Booking> bookings) {
        LocalDate today = LocalDate.now();
        for (Booking booking : bookings) {
            if (booking == null) {
                continue;
            }
            BookingStatus status = booking.getStatus();
            if (status == BookingStatus.CANCELLED || status == BookingStatus.COMPLETED) {
                continue;
            }
            LocalDate endDate = resolveEndDate(booking.getTravelPackage());
            if (endDate != null && endDate.isBefore(today)) {
                booking.setStatus(BookingStatus.COMPLETED);
            }
        }
    }

    private LocalDate resolveEndDate(TravelPackage pack) {
        if (pack == null) {
            return null;
        }
        LocalDate endDate = pack.getEndDate();
        if (endDate != null) {
            return endDate;
        }
        LocalDate startDate = pack.getStartDate();
        if (startDate != null && pack.getDurationDays() > 0) {
            return startDate.plusDays(pack.getDurationDays() - 1L);
        }
        return null;
    }
}
