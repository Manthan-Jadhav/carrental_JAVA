
package com.carrental.car;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public BookingService(
            BookingRepository bookingRepository,
            CarRepository carRepository) {

        this.bookingRepository = bookingRepository;
        this.carRepository = carRepository;
    }

    // ==========================================
    // 1. CHECK CAR AVAILABILITY
    // ==========================================

    public boolean isCarAvailable(
            Integer carId,
            LocalDate pickupDate,
            LocalDate returnDate) {

        // Check missing dates
        if (pickupDate == null || returnDate == null) {
            return false;
        }

        // Return date must be after pickup date
        if (!returnDate.isAfter(pickupDate)) {
            return false;
        }

        // Prevent past pickup dates
        if (pickupDate.isBefore(LocalDate.now())) {
            return false;
        }

        // Check overlapping confirmed bookings
        long overlaps = bookingRepository.countOverlappingBookings(
                carId,
                pickupDate,
                returnDate
        );

        return overlaps == 0;
    }

    // ==========================================
    // 2. CREATE BOOKING
    // ==========================================

    @Transactional
    public Booking createBooking(
            Integer customerId,
            Integer carId,
            LocalDate pickupDate,
            LocalDate returnDate,
            String kmPackage) {

        // 1. Validate booking dates
        if (pickupDate == null || returnDate == null ||
                pickupDate.isBefore(LocalDate.now()) ||
                !returnDate.isAfter(pickupDate)) {

            throw new IllegalArgumentException(
                    "Invalid booking dates."
            );
        }

        // 2. Validate kilometre package
        if (!"standard".equals(kmPackage) &&
                !"unlimited".equals(kmPackage)) {

            throw new IllegalArgumentException(
                    "Invalid kilometre package."
            );
        }

        // 3. Lock car row to prevent simultaneous bookings
        Car car = carRepository.findByIdForUpdate(carId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Car not found."
                        )
                );

        // 4. Check car status
        if (!"Available".equalsIgnoreCase(car.getStatus())) {
            throw new IllegalStateException(
                    "Car is not rentable."
            );
        }

        // 5. Recheck availability while holding the lock
        if (!isCarAvailable(carId, pickupDate, returnDate)) {
            throw new IllegalStateException(
                    "Car is already booked for these dates."
            );
        }

        // 6. Calculate rental duration
        long days = ChronoUnit.DAYS.between(
                pickupDate,
                returnDate
        );

        // 7. Calculate rental price
        BigDecimal dailyRate = car.getPricePerDay();

        BigDecimal rentalAmount = dailyRate.multiply(
                BigDecimal.valueOf(days)
        );

        // 8. Kilometre package
        BigDecimal kmCharge =
                "unlimited".equals(kmPackage)
                        ? BigDecimal.valueOf(4000)
                        : BigDecimal.ZERO;

        // 9. Rental mode
        String rentalMode = days < 7
                ? "Daily Rental"
                : "Subscription";

        // 10. Coverage charge
        BigDecimal coverageCharge = days < 7
                ? BigDecimal.valueOf(559)
                : BigDecimal.valueOf(2000);

        // 11. Security deposit
        BigDecimal depositAmount =
                BigDecimal.valueOf(2000);

        // 12. Total amount
        BigDecimal totalAmount = rentalAmount
                .add(kmCharge)
                .add(coverageCharge)
                .add(depositAmount);

        // 13. Create booking object
        Booking booking = new Booking();

        booking.setCustomerId(customerId);
        booking.setCarId(carId);
        booking.setPickupDate(pickupDate);
        booking.setReturnDate(returnDate);
        booking.setRentalMode(rentalMode);
        booking.setKmPackage(kmPackage);

        booking.setDailyRate(dailyRate);
        booking.setRentalAmount(rentalAmount);
        booking.setKmCharge(kmCharge);
        booking.setCoverageCharge(coverageCharge);
        booking.setDepositAmount(depositAmount);
        booking.setTotalAmount(totalAmount);

        // 14. Initial booking/payment statuses
        booking.setBookingStatus("Pending");
        booking.setPaymentStatus("Pending");

        // 15. Save booking to MySQL
        return bookingRepository.save(booking);
    }



    @Transactional
    public Booking confirmDemoPayment(
            Integer bookingId,
            Integer customerId) {

        // 1. Find the pending booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Booking not found."
                        )
                );

        // 2. Verify booking ownership
        if (!booking.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException(
                    "This booking does not belong to you."
            );
        }

        // 3. Prevent duplicate payment confirmation
        if (!"Pending".equals(booking.getBookingStatus()) ||
                !"Pending".equals(booking.getPaymentStatus())) {
            throw new IllegalStateException(
                    "Booking is already processed."
            );
        }

        // 4. Lock the car row
        Car car = carRepository.findByIdForUpdate(
                booking.getCarId()
        ).orElseThrow(() ->
                new IllegalArgumentException("Car not found.")
        );


        // Re-read the booking after acquiring the car lock
        // so concurrent confirmation requests see the latest status.
        bookingRepository.flush();

        booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found.")
                );

        if (!"Pending".equals(booking.getBookingStatus()) ||
                !"Pending".equals(booking.getPaymentStatus())) {
            throw new IllegalStateException(
                    "Booking has already been processed."
            );
        }


        // 5. Verify car is rentable
        if (!"Available".equalsIgnoreCase(car.getStatus())) {
            throw new IllegalStateException(
                    "Car is not rentable."
            );
        }

        // 6. Recheck availability under the lock
        if (!isCarAvailable(
                booking.getCarId(),
                booking.getPickupDate(),
                booking.getReturnDate())) {

            throw new IllegalStateException(
                    "Car is no longer available for these dates."
            );
        }

        // 7. Simulate successful payment
        booking.setPaymentStatus("Paid");
        booking.setBookingStatus("Confirmed");

        // 8. Save confirmed booking
        return bookingRepository.save(booking);
    }


    @Transactional
    public Booking cancelBooking(
            Integer bookingId,
            Integer customerId) {

        // 1. Find the booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Booking not found."
                        )
                );

        // 2. Verify ownership
        if (!booking.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException(
                    "You cannot cancel this booking."
            );
        }

        // 3. Lock the associated car
        carRepository.findByIdForUpdate(booking.getCarId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Car not found.")
                );

        // 4. Reload booking after acquiring lock
        entityManager.refresh(booking);

        // 5. Prevent repeated cancellation
        if ("Cancelled".equals(booking.getBookingStatus())) {
            throw new IllegalStateException(
                    "Booking is already cancelled."
            );
        }

        // 6. Only allow cancellation before pickup
        if (!booking.getPickupDate().isAfter(LocalDate.now())) {
            throw new IllegalStateException(
                    "Cannot cancel a booking on or after pickup date."
            );
        }

        // 7. Update booking status
        booking.setBookingStatus("Cancelled");

        // Payment status stays unchanged.
        // A paid booking requires a separate refund workflow.

        return bookingRepository.save(booking);
    }

}
