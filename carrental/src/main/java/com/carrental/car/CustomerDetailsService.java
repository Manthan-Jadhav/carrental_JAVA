package com.carrental.car;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomerDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    public CustomerDetailsService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Customer customer = customerRepository
                .findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found with email: " + email));

        String role = customer.getRole();
        if (role == null || role.trim().isEmpty()) {
            role = "CUSTOMER";
        }
        role = role.trim().toUpperCase();

        return User.withUsername(customer.getEmail())
                .password(customer.getPasswordHash())
                .roles(role)
                .build();
    }
}
