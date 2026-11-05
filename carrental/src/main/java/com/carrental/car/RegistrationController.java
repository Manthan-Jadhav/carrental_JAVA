package com.carrental.car;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
public class RegistrationController {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationController(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegistrationPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerCustomer(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String phone,
            Model model) {

        email = email.trim().toLowerCase();

        if (customerRepository.existsByEmail(email)) {
            model.addAttribute("error", "Email already registered!");
            return "register";
        }

        Customer customer = new Customer();
        customer.setName(name);
        customer.setEmail(email);
        customer.setPasswordHash(passwordEncoder.encode(password));
        customer.setPhone(phone);
        customer.setRole("CUSTOMER");

        customerRepository.save(customer);

        return "redirect:/login?registered";
    }
}
