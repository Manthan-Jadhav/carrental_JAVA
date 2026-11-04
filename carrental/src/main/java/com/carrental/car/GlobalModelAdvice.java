package com.carrental.car;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    // @ModelAttribute("isAdmin")
    // public boolean isAdmin(Authentication authentication) {
    //     return authentication != null
    //             && authentication.isAuthenticated()
    //             && authentication.getAuthorities().stream()
    //             .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    // }
}
