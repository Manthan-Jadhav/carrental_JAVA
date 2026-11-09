
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

    

    public boolean isCarAvailable(
            Integer carId,
            LocalDate pickupDate,
            LocalDate returnDate) {

       
        if (pickupDate == null || returnDate == null) {
            return false;
        }

       
        if (!returnDate.isAfter(pickupDate)) {
            return false;
        }

        
        if (pickupDate.isBefore(LocalDate.now())) {
            return false;
        }

       
        long overlaps = bookingRepository.countOverlappingBookings(
                carId,
                pickupDate,
                returnDate
        );

        return overlaps == 0;
    }

 

    @Transactional
    public Booking createBooking(
            Integer customerId,
            Integer carId,
            LocalDate pickupDate,
            LocalDate returnDate,
            String kmPackage) {

       
        if (pickupDate == null || returnDate == null ||
                pickupDate.isBefore(LocalDate.now()) ||
                !returnDate.isAfter(pickupDate)) {

            throw new IllegalArgumentException(
                    "Invalid booking dates."
            );
        }

        
        if (!"standard".equals(kmPackage) &&
                !"unlimited".equals(kmPackage)) {

            throw new IllegalArgumentException(
                    "Invalid kilometre package."
            );
        }

        
        Car car = carRepository.findByIdForUpdate(carId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Car not found."
                        )
                );

        
        if (!"Available".equalsIgnoreCase(car.getStatus())) {
            throw new IllegalStateException(
                    "Car is not rentable."
            );
        }

        
        if (!isCarAvailable(carId, pickupDate, returnDate)) {
            throw new IllegalStateException(
                    "Car is already booked for these dates."
            );
        }

        
        long days = ChronoUnit.DAYS.between(
                pickupDate,
                returnDate
        );

        
        BigDecimal dailyRate = car.getPricePerDay();

        BigDecimal rentalAmount = dailyRate.multiply(
                BigDecimal.valueOf(days)
        );

        // basic  - unlimited
        BigDecimal kmCharge =
                "unlimited".equals(kmPackage)
                        ? BigDecimal.valueOf(4000)
                        : BigDecimal.ZERO;

        // 559 & 2000 
        String rentalMode = days < 7
                ? "Daily Rental"
                : "Subscription";

        
        BigDecimal coverageCharge = days < 7
                ? BigDecimal.valueOf(559)
                : BigDecimal.valueOf(2000);

        
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

        // Initial  statuses
        booking.setBookingStatus("Pending");
        booking.setPaymentStatus("Pending");

        // MySQL
        return bookingRepository.save(booking);
    }



    @Transactional
    public Booking confirmDemoPayment(
            Integer bookingId,
            Integer customerId) {

       
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Booking not found."
                        )
                );

       
        if (!booking.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException(
                    "This booking does not belong to you."
            );
        }

        
        if (!"Pending".equals(booking.getBookingStatus()) ||
                !"Pending".equals(booking.getPaymentStatus())) {
            throw new IllegalStateException(
                    "Booking is already processed."
            );
        }

        Car car = carRepository.findByIdForUpdate(
                booking.getCarId()
        ).orElseThrow(() ->
                new IllegalArgumentException("Car not found.")
        );


    
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


       
        if (!"Available".equalsIgnoreCase(car.getStatus())) {
            throw new IllegalStateException(
                    "Car is not rentable."
            );
        }

      
        if (!isCarAvailable(
                booking.getCarId(),
                booking.getPickupDate(),
                booking.getReturnDate())) {

            throw new IllegalStateException(
                    "Car is no longer available for these dates."
            );
        }

      
        booking.setPaymentStatus("Paid");
        booking.setBookingStatus("Confirmed");

        
        return bookingRepository.save(booking);
    }


    @Transactional
    public Booking cancelBooking(
            Integer bookingId,
            Integer customerId) {

    
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Booking not found."
                        )
                );

        
        if (!booking.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException(
                    "You cannot cancel this booking."
            );
        }

        
        carRepository.findByIdForUpdate(booking.getCarId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Car not found.")
                );

        
        entityManager.refresh(booking);

        
        if ("Cancelled".equals(booking.getBookingStatus())) {
            throw new IllegalStateException(
                    "Booking is already cancelled."
            );
        }

        
        if (!booking.getPickupDate().isAfter(LocalDate.now())) {
            throw new IllegalStateException(
                    "Cannot cancel a booking on or after pickup date."
            );
        }

       
        booking.setBookingStatus("Cancelled");

      

        return bookingRepository.save(booking);
    }

}
