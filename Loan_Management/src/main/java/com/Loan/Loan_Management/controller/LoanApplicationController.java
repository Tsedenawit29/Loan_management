package com.Loan.Loan_Management.controller;

import com.Loan.Loan_Management.Service.LoanApplicationService;
import com.Loan.Loan_Management.config.security.CustomUserDetail;
import com.Loan.Loan_Management.dto.LoanApplicationRequest;
import com.Loan.Loan_Management.dto.LoanApplicationResponse;
import com.Loan.Loan_Management.Security.Services.CustomDetailService; // <--- NEW EXPLICIT IMPORT FOR YOUR CUSTOM USERDETAILS
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails; // Still needed for the argument type
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<LoanApplicationResponse> applyForLoan(
            @Valid @RequestBody LoanApplicationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Cast UserDetails to your specific CustomUserDetails to access the ID
        Long userId = ((CustomUserDetail) userDetails).getId(); // <--- Direct cast is now cleaner with import

        try {
            LoanApplicationResponse newApplication = loanApplicationService.applyForLoan(request, userId);
            return new ResponseEntity<>(newApplication, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<LoanApplicationResponse>> getMyLoanApplications(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = ((com.Loan.Loan_Management.config.security.CustomUserDetail) userDetails).getId(); // <--- Direct cast

        List<LoanApplicationResponse> applications = loanApplicationService.getLoanApplicationsByCustomer(userId);
        return new ResponseEntity<>(applications, HttpStatus.OK);
    }

    @GetMapping("/my-applications/{loanId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<LoanApplicationResponse> getMyLoanApplicationById(
            @PathVariable Long loanId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = ((CustomUserDetail) userDetails).getId(); // <--- Direct cast

        try {
            LoanApplicationResponse application = loanApplicationService.getLoanApplicationByIdForCustomer(loanId, userId);
            return new ResponseEntity<>(application, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('LOAN_OFFICER')")
    public ResponseEntity<List<LoanApplicationResponse>> getAllPendingApplications() {
        List<LoanApplicationResponse> applications = loanApplicationService.getAllPendingApplications();
        return new ResponseEntity<>(applications, HttpStatus.OK);
    }

    @GetMapping("/{loanId}")
    @PreAuthorize("hasRole('LOAN_OFFICER')")
    public ResponseEntity<LoanApplicationResponse> getLoanApplicationDetails(@PathVariable Long loanId) {
        try {
            LoanApplicationResponse application = loanApplicationService.getLoanApplicationById(loanId);
            return new ResponseEntity<>(application, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}