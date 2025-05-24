package com.Loan.Loan_Management.controller;

import com.Loan.Loan_Management.Service.LoanApplicationService;
import com.Loan.Loan_Management.Service.UserService;
import com.Loan.Loan_Management.dto.LoanApplicationRequest;
import com.Loan.Loan_Management.dto.LoanApplicationResponse; // Ensure this is imported
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
import org.springframework.web.bind.annotation.PathVariable; // <-- IMPORTANT: Added this import
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/customer")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
public class CustomerController {

    private final LoanApplicationService loanApplicationService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String customerDashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        Users currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + username));

        List<LoanApplicationResponse> customerLoans = loanApplicationService.getLoansByCustomerId(currentUser.getId());
        model.addAttribute("loans", customerLoans);
        model.addAttribute("username", username);
        return "customer/dashboard";
    }

    @GetMapping("/apply")
    public String showLoanApplicationForm(Model model) {
        model.addAttribute("loanApplicationRequest", new LoanApplicationRequest());
        return "customer/apply";
    }

    @PostMapping("/apply")
    public String submitLoanApplication(@Valid @ModelAttribute("loanApplicationRequest") LoanApplicationRequest request,
                                        BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes,
                                        Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return "customer/apply";
        }

        try {
            String username = authentication.getName();
            Users currentUser = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found. Cannot submit loan."));

            loanApplicationService.applyForLoan(currentUser.getId(), request);

            redirectAttributes.addFlashAttribute("successMessage", "Loan application submitted successfully!");
            return "redirect:/customer/dashboard";
        } catch (Exception e) {
            bindingResult.reject(null, "Error submitting loan application: " + e.getMessage());
            return "customer/apply";
        }
    }

    // --- NEW METHOD FOR CUSTOMER LOAN DETAILS ---
    @GetMapping("/loans/{id}")
    public String viewCustomerLoanDetails(@PathVariable("id") Long loanId, Model model, Authentication authentication) {
        String username = authentication.getName();
        Users currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + username));

        try {
            // Retrieve the loan using both loanId and customerId for security
            LoanApplicationResponse loan = loanApplicationService.getLoanApplicationByIdAndCustomerId(loanId, currentUser.getId());
            model.addAttribute("loan", loan);
            return "customer/loan_details"; // This will render src/main/resources/templates/customer/loan_details.html
        } catch (RuntimeException e) { // Catch the exception if loan not found or not belonging to user
            model.addAttribute("errorMessage", "Loan not found or you do not have permission to view it.");
            // Redirect back to dashboard with an error message in case of issues
            return "redirect:/customer/dashboard";
        }
    }
}