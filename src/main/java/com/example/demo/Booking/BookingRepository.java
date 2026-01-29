package com.example.demo.Booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
        select coalesce(sum(b.travelersCount), 0)
        from Booking b
        where b.travelPackage.id = :packId
    """)
    Integer sumTravelersByPackId(@Param("packId") Long packId);

    boolean existsByStripeSessionId(String stripeSessionId);

    List<Booking> findByAppUser_UsernameOrderByCreatedAtDesc(String username);

    List<Booking> findByEmailOrderByCreatedAtDesc(String email);

    @Query("""
        select coalesce(sum(b.travelersCount * p.price), 0)
        from Booking b
        join b.travelPackage p
        join p.businessUser bu
        where bu.id = :businessId
    """)
    java.math.BigDecimal sumEarningsByBusinessId(@Param("businessId") Long businessId);

    @org.springframework.data.jpa.repository.Modifying
    @Query(value = """
        update bookings
        set status = 'CONFIRMED'
        where status is null or status = 'PENDING'
        """, nativeQuery = true)
    int normalizeStatuses();

    @Query("""
        select p.id, coalesce(sum(b.travelersCount * coalesce(p.price, 0)), 0)
        from Booking b
        join b.travelPackage p
        join p.businessUser bu
        where bu.id = :businessId
        group by p.id
    """)
    List<Object[]> sumEarningsByPackForBusiness(@Param("businessId") Long businessId);
}

