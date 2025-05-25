package com.Loan.Loan_Management.controller;

import com.Loan.Loan_Management.Service.LoanApplicationService;
import com.Loan.Loan_Management.Service.UserService;
import com.Loan.Loan_Management.dto.LoanApplicationResponse;
import com.Loan.Loan_Management.dto.LoanDecisionRequest;
import com.Loan.Loan_Management.Entity.LoanStatus;
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
@RequestMapping("/officer") // This is the base path for ALL officer actions
@PreAuthorize("hasRole('LOAN_OFFICER')") // Corrected to LOAN_OFFICER
@RequiredArgsConstructor
public class OfficerController {

    private final LoanApplicationService loanApplicationService;
    private final UserService userService;

    // Officer Dashboard
    @GetMapping("/dashboard")
    public String officerDashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        model.addAttribute("username", username);
        model.addAttribute("pendingLoansCount", loanApplicationService.getLoansByStatus(LoanStatus.PENDING).size());
        model.addAttribute("approvedLoansCount", loanApplicationService.getLoansByStatus(LoanStatus.APPROVED).size());
        model.addAttribute("rejectedLoansCount", loanApplicationService.getLoansByStatus(LoanStatus.REJECTED).size());
        return "officer/dashboard";
    }

    // List all pending loan applications for review
    @GetMapping("/loans/pending")
    public String viewPendingLoans(Model model) {
        List<LoanApplicationResponse> pendingLoans = loanApplicationService.getLoansByStatus(LoanStatus.PENDING);
        model.addAttribute("loans", pendingLoans);
        model.addAttribute("statusFilter", "Pending");
        return "officer/loan_list_filtered"; // Reusing a general loan list template
    }

    // List all approved loan applications
    @GetMapping("/loans/approved")
    public String viewApprovedLoans(Model model) {
        List<LoanApplicationResponse> approvedLoans = loanApplicationService.getLoansByStatus(LoanStatus.APPROVED);
        model.addAttribute("loans", approvedLoans);
        model.addAttribute("statusFilter", "Approved");
        return "officer/loan_list_filtered";
    }

    // List all rejected loan applications
    @GetMapping("/loans/rejected") // Note the full path relative to /officer
    public String viewRejectedLoans(Model model) {
        List<LoanApplicationResponse> rejectedLoans = loanApplicationService.getLoansByStatus(LoanStatus.REJECTED);
        model.addAttribute("loans", rejectedLoans);
        model.addAttribute("statusFilter", "Rejected");
        return "officer/loan_list_filtered";
    }

    // List all loan applications
    @GetMapping("/loans/all")
    public String viewAllLoans(Model model) {
        List<LoanApplicationResponse> allLoans = loanApplicationService.getAllLoanApplications();
        model.addAttribute("loans", allLoans);
        model.addAttribute("statusFilter", "All");
        return "officer/loan_list_filtered";
    }

    // Display details of a specific loan application for officer review
    @GetMapping("/loans/{id}")
    public String viewLoanDetails(@PathVariable("id") Long loanId, Model model) {
        try {
            LoanApplicationResponse loan = loanApplicationService.getLoanApplicationById(loanId);
            model.addAttribute("loan", loan);
            model.addAttribute("loanDecisionRequest", new LoanDecisionRequest());
            return "officer/loan_details";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Loan not found: " + e.getMessage());
            return "redirect:/officer/dashboard";
        }
    }

    // Handle loan approval
    @PostMapping("/loans/{id}/approve")
    public String approveLoan(@PathVariable("id") Long loanId,
                              @Valid @ModelAttribute("loanDecisionRequest") LoanDecisionRequest request,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Approval notes are required.");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.loanDecisionRequest", bindingResult);
            redirectAttributes.addFlashAttribute("loanDecisionRequest", request);
            return "redirect:/officer/loans/" + loanId;
        }

        try {
            loanApplicationService.approveLoan(loanId, request);
            redirectAttributes.addFlashAttribute("successMessage", "Loan application ID " + loanId + " approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error approving loan ID " + loanId + ": " + e.getMessage());
        }
        return "redirect:/officer/loans/" + loanId;
    }

    // Handle loan rejection
    @PostMapping("/loans/{id}/reject")
    public String rejectLoan(@PathVariable("id") Long loanId,
                             @Valid @ModelAttribute("loanDecisionRequest") LoanDecisionRequest request,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Rejection notes are required.");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.loanDecisionRequest", bindingResult);
            redirectAttributes.addFlashAttribute("loanDecisionRequest", request);
            return "redirect:/officer/loans/" + loanId;
        }

        try {
            loanApplicationService.rejectLoan(loanId, request);
            redirectAttributes.addFlashAttribute("successMessage", "Loan application ID " + loanId + " rejected successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error rejecting loan ID " + loanId + ": " + e.getMessage());
        }
        return "redirect:/officer/loans/" + loanId;
    }
}