package com.Loan.Loan_Management.controller;

import com.Loan.Loan_Management.Service.LoanApplicationService;
import com.Loan.Loan_Management.dto.LoanApplicationRequest;
import com.Loan.Loan_Management.dto.LoanApplicationResponse;
import com.Loan.Loan_Management.dto.LoanDecisionRequest; // Make sure this is imported
import com.Loan.Loan_Management.config.security.CustomUserDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
        Long userId = ((CustomUserDetail) userDetails).getId();
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
        Long userId = ((CustomUserDetail) userDetails).getId();
        List<LoanApplicationResponse> applications = loanApplicationService.getLoanApplicationsByCustomer(userId);
        return new ResponseEntity<>(applications, HttpStatus.OK);
    }

    @GetMapping("/my-applications/{loanId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<LoanApplicationResponse> getMyLoanApplicationById(
            @PathVariable Long loanId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = ((CustomUserDetail) userDetails).getId();
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

    @PostMapping("/{loanId}/approve")
    @PreAuthorize("hasRole('LOAN_OFFICER')")
    public ResponseEntity<LoanApplicationResponse> approveLoan(
            @PathVariable Long loanId,
            @Valid @RequestBody LoanDecisionRequest request
    ) {
        try {
            LoanApplicationResponse updatedApplication = loanApplicationService.approveLoan(loanId, request);
            return new ResponseEntity<>(updatedApplication, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{loanId}/reject")
    @PreAuthorize("hasRole('LOAN_OFFICER')")
    public ResponseEntity<LoanApplicationResponse> rejectLoan(
            @PathVariable Long loanId,
            @Valid @RequestBody LoanDecisionRequest request
    ) {
        try {
            LoanApplicationResponse updatedApplication = loanApplicationService.rejectLoan(loanId, request);
            return new ResponseEntity<>(updatedApplication, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}