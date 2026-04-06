package com.studyplanner.web;

import com.studyplanner.domain.UserRole;
import com.studyplanner.security.CustomUserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        UserRole role = CustomUserDetailsService.roleFromAuthorities(principal);
        return switch (role) {
            case STUDENT -> "redirect:/student";
            case TUTOR -> "redirect:/tutor";
            case ADMIN -> "redirect:/admin";
        };
    }
}
