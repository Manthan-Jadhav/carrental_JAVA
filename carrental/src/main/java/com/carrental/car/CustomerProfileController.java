package com.carrental.car;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CustomerProfileController {

    private final CustomerRepository customerRepository;

    public CustomerProfileController(
            CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/profile")
    public String showProfile(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        Customer customer = customerRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Logged-in customer not found"
                        )
                );

        model.addAttribute("customer", customer);

        return "profile";
    }

    @PostMapping("/profile/license")
    public String updateDrivingLicense(
            Authentication authentication,
            @RequestParam("drivingLicenseNo") String drivingLicenseNo,
            RedirectAttributes redirectAttributes) {

        String email = authentication.getName();

        Customer customer = customerRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Logged-in customer not found"
                        )
                );

        customer.setDrivingLicenseNo(drivingLicenseNo != null ? drivingLicenseNo.trim() : null);
        customerRepository.save(customer);

        redirectAttributes.addFlashAttribute("successMessage", "Driving licence number saved successfully!");

        return "redirect:/profile";
    }
}
