
package com.carrental.car;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@Controller
public class MyBookingsController {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final JdbcTemplate jdbcTemplate;

    public MyBookingsController(
            BookingRepository bookingRepository,
            CustomerRepository customerRepository,
            JdbcTemplate jdbcTemplate) {

        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/my-bookings")
    public String myBookings(
            Authentication authentication,
            Model model) {

        
        String email = authentication.getName();

        Customer customer = customerRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Customer not found."
                        )
                );

        List<Booking> bookings = bookingRepository
                .findByCustomerIdOrderByCreatedAtDesc(
                        customer.getCustomerId()
                );

       
        for (Booking booking : bookings) {

            Car car = booking.getCar();

            List<String> images = jdbcTemplate.query(
                    "SELECT image_url FROM CAR_IMAGE " +
                            "WHERE car_id = ? LIMIT 1",
                    (rs, rowNum) -> rs.getString("image_url"),
                    car.getCarId()
            );

            String imageUrl = images.isEmpty()
                    ? "/img/swift_front.jpg"
                    : images.get(0);

            if (!imageUrl.startsWith("/") &&
                    !imageUrl.startsWith("http")) {

                imageUrl = "/img/" + imageUrl;
            }

            car.setImageUrl(imageUrl);
        }

       
        model.addAttribute("bookings", bookings);

        return "my-bookings";
    }
}
