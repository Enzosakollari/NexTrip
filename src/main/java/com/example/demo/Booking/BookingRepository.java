package com.example.demo.Booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByAppUser_IdOrderByCreatedAtDesc(Long userId);

    List<Booking> findByTravelPackage_BusinessUser_IdOrderByCreatedAtDesc(Long businessId);

    boolean existsByIdAndTravelPackage_BusinessUser_Id(Long bookingId, Long businessId);

        @Query("""
        select b
        from Booking b
        join b.travelPackage p
        join p.businessUser bu
        where bu.id = :businessId
        order by b.createdAt desc
    """)
        List<Booking> findAllByBusinessId(Long businessId);

        @Query("""
        select b
        from Booking b
        join b.travelPackage p
        join p.businessUser bu
        where b.id = :bookingId and bu.id = :businessId
    """)
        Optional<Booking> findByIdAndBusinessId(Long bookingId, Long businessId);
}

