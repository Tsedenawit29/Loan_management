package com.Loan.Loan_Management.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String home() {
        return "index"; // Renders src/main/resources/templates/index.html
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // Renders src/main/resources/templates/login.html
    }

    @GetMapping("/register")
    public String register() {
        return "register"; // Renders src/main/resources/templates/register.html
    }

    @GetMapping("/default-dashboard")
    public String defaultDashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
            return "redirect:/customer/dashboard"; // Redirect customer to their dashboard
        } else if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_LOAN_OFFICER"))) {
            return "redirect:/officer/dashboard"; // Redirect officer to their dashboard
        }
        // Fallback if no specific role or not authenticated (should not happen if anyRequest().authenticated() works)
        return "redirect:/";
    }
}