package com.Loan.Loan_Management.controller;

import com.Loan.Loan_Management.Service.LoanApplicationService;
import com.Loan.Loan_Management.Service.UserService;
import com.Loan.Loan_Management.dto.LoanApplicationRequest;
import com.Loan.Loan_Management.dto.LoanApplicationResponse;
import com.Loan.Loan_Management.Entity.Users;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/customer/loans")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;
    private final UserService userService;

    // Display form for a new loan application
    @GetMapping("/apply")
    public String showLoanApplicationForm(Model model) {
        model.addAttribute("loanApplicationRequest", new LoanApplicationRequest());
        return "customer/apply_loan"; // Thymeleaf template for the application form
    }
    @PostMapping("/apply")
    public String submitLoanApplication(@Valid @ModelAttribute("loanApplicationRequest") LoanApplicationRequest request,
                                        BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes,
                                        Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return "customer/apply_loan";
        }

        try {
            String username = authentication.getName();
            Users currentUser = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found. Cannot submit loan."));

            loanApplicationService.applyForLoan(currentUser.getId(), request);

            redirectAttributes.addFlashAttribute("successMessage", "Loan application submitted successfully!");
            // FIX: Redirect to the customer dashboard instead of /my-loans
            return "redirect:/customer/dashboard";
        } catch (Exception e) {
            bindingResult.reject(null, "Error submitting loan application: " + e.getMessage());
            return "customer/apply_loan";
        }
    }
    @GetMapping("/my-loans")
    public String listCustomerLoans(Model model, Authentication authentication) {
        try {
            String username = authentication.getName();
            Users currentUser = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + username));

            List<LoanApplicationResponse> customerLoans = loanApplicationService.getLoansByCustomerId(currentUser.getId());
            model.addAttribute("customerLoans", customerLoans); // Note: Dashboard uses 'loans'
            return "customer/my_loans"; // This template *must* exist if you keep this endpoint
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Error fetching your loans: " + e.getMessage());
            return "customer/my_loans";
        }
    }

    // Display details of a specific loan application for the logged-in customer
    @GetMapping("/{id}")
    public String viewCustomerLoanDetails(@PathVariable("id") Long loanId, Model model, Authentication authentication) {
        try {
            String username = authentication.getName();
            Users currentUser = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + username));

            LoanApplicationResponse loan = loanApplicationService.getLoanApplicationByIdAndCustomerId(loanId, currentUser.getId());
            model.addAttribute("loan", loan);
            return "customer/loan_details";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Loan not found or does not belong to your account.");
            return "redirect:/customer/loans/my-loans"; // Redirect to customer's loan list (or dashboard if `/my-loans` removed)
        }
    }
}