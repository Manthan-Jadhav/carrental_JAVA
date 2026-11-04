
package com.carrental.car;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.time.LocalDate;

public interface BookingRepository
        extends JpaRepository<Booking, Integer> {

    // 1. Fetch customer's bookings with car details
    @EntityGraph(attributePaths = "car")
    List<Booking> findByCustomerIdOrderByCreatedAtDesc(
            Integer customerId
    );

    // 2. Check overlapping confirmed bookings
    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.carId = :carId
        AND b.bookingStatus = 'Confirmed'
        AND b.pickupDate < :returnDate
        AND b.returnDate > :pickupDate
    """)
    long countOverlappingBookings(
            @Param("carId") Integer carId,
            @Param("pickupDate") LocalDate pickupDate,
            @Param("returnDate") LocalDate returnDate
    );
}
