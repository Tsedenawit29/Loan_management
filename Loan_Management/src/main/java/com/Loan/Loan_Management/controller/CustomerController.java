package com.Loan.Loan_Management.controller;

import com.Loan.Loan_Management.Service.UserService;
import com.Loan.Loan_Management.Service.LoanApplicationService;
import com.Loan.Loan_Management.Entity.Users;
import com.Loan.Loan_Management.dto.LoanApplicationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/customer") // Base path for general customer actions
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
public class CustomerController {

    private final UserService userService;
    private final LoanApplicationService loanApplicationService;

    @GetMapping("/dashboard")
    public String customerDashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        Users currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + username));
        List<LoanApplicationResponse> customerLoans = loanApplicationService.getLoansByCustomerId(currentUser.getId());
        model.addAttribute("loans", customerLoans);
        model.addAttribute("username", username);
        return "customer/dashboard"; // Thymeleaf template for the customer dashboard
    }
}