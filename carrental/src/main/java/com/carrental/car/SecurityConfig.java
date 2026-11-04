package com.carrental.car;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

//     @Bean
//     public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
//         return (request, response, authentication) -> {
//             boolean isAdmin = authentication.getAuthorities().stream()
//                     .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
//             if (isAdmin) {
//                 response.sendRedirect("/admin");
//             } else {
//                 response.sendRedirect("/profile");
//             }
//         };
//     }
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/", "/cars", "/cars/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/cars", "/").authenticated()
                        .requestMatchers(
                                "/register",
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/booking/check-availability"
                        ).permitAll()
//                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/profile", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}
