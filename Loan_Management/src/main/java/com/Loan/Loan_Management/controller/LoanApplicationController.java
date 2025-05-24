package com.Loan.Loan_Management.controller;

import com.Loan.Loan_Management.Service.LoanApplicationService;
import com.Loan.Loan_Management.dto.LoanApplicationResponse;
import com.Loan.Loan_Management.dto.LoanDecisionRequest; // Make sure this is imported
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/officer/loans") // Path for loan officer related loan actions
@PreAuthorize("hasRole('LOAN_OFFICER')") // Only Loan Officers can access this controller
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    // Display all pending loan applications for review
    @GetMapping("/pending")
    public String listPendingApplications(Model model) {
        List<LoanApplicationResponse> pendingLoans = loanApplicationService.getAllPendingApplications();
        model.addAttribute("pendingLoans", pendingLoans);
        return "officer/pending_loans"; // Thymeleaf template: src/main/resources/templates/officer/pending_loans.html
    }

    // Display details of a specific loan application
    @GetMapping("/{id}")
    public String viewLoanDetails(@PathVariable("id") Long loanId, Model model) {
        try {
            LoanApplicationResponse loan = loanApplicationService.getLoanApplicationById(loanId);
            model.addAttribute("loan", loan);
            model.addAttribute("loanDecisionRequest", new LoanDecisionRequest()); // Initialize for the decision form
            return "officer/loan_details"; // Thymeleaf template: src/main/resources/templates/officer/loan_details.html
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            // It's better to redirect or show a proper error page
            return "error/404"; // You might have a generic error page
        }
    }

    // Handle loan approval
    @PostMapping("/{id}/approve")
    public String approveLoan(@PathVariable("id") Long loanId,
                              @Valid @ModelAttribute("loanDecisionRequest") LoanDecisionRequest request,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            // If validation fails (e.g., notes are empty), redirect back to loan details with an error
            redirectAttributes.addFlashAttribute("errorMessage", "Approval notes cannot be empty.");
            return "redirect:/officer/loans/" + loanId;
        }

        try {
            // Corrected call: Pass loanId first, then the request DTO
            loanApplicationService.approveLoan(loanId, request);
            redirectAttributes.addFlashAttribute("successMessage", "Loan application ID " + loanId + " approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error approving loan ID " + loanId + ": " + e.getMessage());
        }
        return "redirect:/officer/loans/pending"; // Redirect to pending list after decision
    }

    // Handle loan rejection
    @PostMapping("/{id}/reject")
    public String rejectLoan(@PathVariable("id") Long loanId,
                             @Valid @ModelAttribute("loanDecisionRequest") LoanDecisionRequest request,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Rejection notes cannot be empty.");
            return "redirect:/officer/loans/" + loanId;
        }

        try {
            // Corrected call: Pass loanId first, then the request DTO
            loanApplicationService.rejectLoan(loanId, request);
            redirectAttributes.addFlashAttribute("successMessage", "Loan application ID " + loanId + " rejected successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error rejecting loan ID " + loanId + ": " + e.getMessage());
        }
        return "redirect:/officer/loans/pending";
    }

    // Optionally, a general list of all loans (approved, rejected, pending)
    @GetMapping("/all")
    public String listAllApplications(Model model) {
        // You'll need a method in LoanApplicationService to get all loans
        // List<LoanApplicationResponse> allLoans = loanApplicationService.getAllLoans();
        // model.addAttribute("allLoans", allLoans);
        return "officer/all_loans"; // Placeholder
    }
}