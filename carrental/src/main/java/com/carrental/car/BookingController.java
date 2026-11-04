
package com.carrental.car;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;



import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;

@Controller
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;
    private final CustomerRepository customerRepository;

    public BookingController(
            BookingService bookingService,
            CustomerRepository customerRepository) {

        this.bookingService = bookingService;
        this.customerRepository = customerRepository;
    }

    // ==========================================
    // 1. CHECK CAR AVAILABILITY
    // ==========================================

    @GetMapping("/check-availability")
    @ResponseBody
    public ResponseEntity<Boolean> checkAvailability(
            @RequestParam Integer carId,
            @RequestParam LocalDate pickupDate,
            @RequestParam LocalDate returnDate) {

        boolean available = bookingService.isCarAvailable(
                carId,
                pickupDate,
                returnDate
        );

        return ResponseEntity.ok(available);
    }

    // ==========================================
    // 2. CREATE PENDING BOOKING
    // ==========================================

    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createBooking(
            Authentication authentication,
            @RequestParam Integer carId,
            @RequestParam LocalDate pickupDate,
            @RequestParam LocalDate returnDate,
            @RequestParam String kmPackage) {

        // Verify authenticated customer
        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity.status(401)
                    .body("Please log in to book a car.");
        }

        try {
            // Find logged-in customer
            Customer customer = customerRepository
                    .findByEmail(authentication.getName())
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Customer not found."
                            )
                    );

            // Create pending booking
            Booking booking = bookingService.createBooking(
                    customer.getCustomerId(),
                    carId,
                    pickupDate,
                    returnDate,
                    kmPackage
            );

            return ResponseEntity.ok(
                    "Pending booking created. ID: " +
                            booking.getBookingId()
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());

        } catch (IllegalStateException e) {

            return ResponseEntity.status(409)
                    .body(e.getMessage());
        }
    }


    @PostMapping("/confirm")
    @ResponseBody
    public ResponseEntity<?> confirmBooking(
            Authentication authentication,
            @RequestParam Integer bookingId) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity.status(401)
                    .body("Please log in first.");
        }

        try {
            // Identify the logged-in customer
            Customer customer = customerRepository
                    .findByEmail(authentication.getName())
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Customer not found."
                            )
                    );

            // Simulate payment and confirm reservation
            Booking booking = bookingService.confirmDemoPayment(
                    bookingId,
                    customer.getCustomerId()
            );

            return ResponseEntity.ok(
                    "Demo payment successful! Booking #" +
                            booking.getBookingId() +
                            " is now Confirmed."
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());

        } catch (IllegalStateException e) {

            return ResponseEntity.status(409)
                    .body(e.getMessage());
        }
    }


    @PostMapping("/cancel")
    @ResponseBody
    public ResponseEntity<String> cancelBooking(
            @RequestParam Integer bookingId,
            Authentication authentication) {

        try {
            // Identify the logged-in customer
            Customer customer = customerRepository
                    .findByEmail(authentication.getName())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Customer not found."
                            )
                    );

            // Cancel their booking
            bookingService.cancelBooking(
                    bookingId,
                    customer.getCustomerId()
            );

            return ResponseEntity.ok(
                    "Booking #" + bookingId +
                            " cancelled successfully."
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(409)
                    .body(e.getMessage());
        }
    }

}
