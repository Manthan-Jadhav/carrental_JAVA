// package com.carrental.car;

// import org.springframework.boot.ApplicationArguments;
// import org.springframework.boot.ApplicationRunner;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Component;

// @Component
// public class AdminDataInitializer implements ApplicationRunner {

//     private final CustomerRepository customerRepository;
//     private final PasswordEncoder passwordEncoder;

//     public AdminDataInitializer(
//             CustomerRepository customerRepository,
//             PasswordEncoder passwordEncoder) {
//         this.customerRepository = customerRepository;
//         this.passwordEncoder = passwordEncoder;
//     }

//     @Override
//     public void run(ApplicationArguments args) {
//         try {
//             String adminEmail = "admin@carrental.com";
//             if (customerRepository.findByEmail(adminEmail).isEmpty()) {
//                 Customer admin = new Customer();
//                 admin.setName("System Admin");
//                 admin.setEmail(adminEmail);
//                 admin.setPasswordHash(passwordEncoder.encode("admin123"));
//                 admin.setPhone("9999999999");
//                 admin.setDrivingLicenseNo("ADMIN-DL-001");
//                 customerRepository.save(admin);
//             }
//         } catch (Exception e) {
//             System.err.println("AdminDataInitializer notice: " + e.getMessage());
//         }
//     }
// }
