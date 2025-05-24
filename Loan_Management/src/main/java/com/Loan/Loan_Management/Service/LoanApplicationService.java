package com.Loan.Loan_Management.Service;

import com.Loan.Loan_Management.Entity.LoanApplication;
import com.Loan.Loan_Management.Entity.LoanStatus; // Correct import for LoanStatus enum
import com.Loan.Loan_Management.Entity.Users;
import com.Loan.Loan_Management.Repository.LoanApplicationRepository;
import com.Loan.Loan_Management.Repository.UserRepository;
import com.Loan.Loan_Management.dto.LoanApplicationRequest;
import com.Loan.Loan_Management.dto.LoanApplicationResponse;
import com.Loan.Loan_Management.dto.LoanDecisionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value; // Import for @Value

import java.math.BigDecimal;
import java.math.RoundingMode; // For precise calculations
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRepository userRepository;

    @Value("${loan.interest-rate:0.12}") // Default annual interest rate (e.g., 12%)
    private BigDecimal annualInterestRate;

    // *** MISSING METHOD: applyForLoan ***
    @Transactional
    public LoanApplicationResponse applyForLoan(LoanApplicationRequest request, Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        LoanApplication loanApplication = LoanApplication.builder()
                .user(user)
                .loanAmount(request.getLoanAmount())
                .loanType(request.getLoanType())
                .durationMonths(request.getDurationMonths())
                .purpose(request.getPurpose())
                .annualIncome(request.getAnnualIncome())
                .status(LoanStatus.PENDING) // Initial status
                .applicationDate(LocalDateTime.now())
                .build();

        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);
        return LoanApplicationResponse.fromEntity(savedApplication);
    }

    // *** MISSING METHOD: getLoanApplicationsByCustomer ***
    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getLoanApplicationsByCustomer(Long userId) {
        List<LoanApplication> applications = loanApplicationRepository.findByUser_Id(userId);
        return applications.stream()
                .map(LoanApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // *** MISSING METHOD: getLoanApplicationByIdForCustomer ***
    @Transactional(readOnly = true)
    public LoanApplicationResponse getLoanApplicationByIdForCustomer(Long loanId, Long userId) {
        LoanApplication loanApplication = loanApplicationRepository.findByIdAndUser_Id(loanId, userId)
                .orElseThrow(() -> new RuntimeException("Loan application not found or does not belong to user."));
        return LoanApplicationResponse.fromEntity(loanApplication);
    }

    // *** MISSING METHOD: getLoanApplicationById (used by officer for specific loan) ***
    @Transactional(readOnly = true)
    public LoanApplicationResponse getLoanApplicationById(Long loanId) {
        return loanApplicationRepository.findById(loanId)
                .map(LoanApplicationResponse::fromEntity)
                .orElseThrow(() -> new RuntimeException("Loan application not found with ID: " + loanId));
    }


    // Existing method: getAllPendingApplications (ensure this is correct if it was there)
    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getAllPendingApplications() {
        return loanApplicationRepository.findByStatus(LoanStatus.PENDING).stream()
                .map(LoanApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }


    // NEW: Approve Loan (Modified for flexible status)
    @Transactional
    public LoanApplicationResponse approveLoan(Long loanId, LoanDecisionRequest request) {
        LoanApplication loan = loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan application not found with ID: " + loanId));

        // Allow approval if currently PENDING or REJECTED
        if (loan.getStatus() != LoanStatus.PENDING && loan.getStatus() != LoanStatus.REJECTED) {
            throw new IllegalStateException("Loan application cannot be APPROVED from current status: " + loan.getStatus());
        }

        loan.setStatus(LoanStatus.APPROVED);
        loan.setOfficerNotes(request.getNotes());
        loan.setDecisionDate(LocalDateTime.now());
        loan.setApprovalDate(LocalDateTime.now()); // Set approval date

        // Calculate and set EMI, interest rate, start/end dates
        calculateAndSetLoanDetails(loan);

        LoanApplication updatedLoan = loanApplicationRepository.save(loan);
        return LoanApplicationResponse.fromEntity(updatedLoan);
    }

    // NEW: Reject Loan (Modified for flexible status)
    @Transactional
    public LoanApplicationResponse rejectLoan(Long loanId, LoanDecisionRequest request) {
        LoanApplication loan = loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan application not found with ID: " + loanId));

        // Allow rejection if currently PENDING or APPROVED
        if (loan.getStatus() != LoanStatus.PENDING && loan.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException("Loan application cannot be REJECTED from current status: " + loan.getStatus());
        }

        loan.setStatus(LoanStatus.REJECTED);
        loan.setOfficerNotes(request.getNotes());
        loan.setDecisionDate(LocalDateTime.now());
        // When rejected, reset any approval-related fields
        loan.setApprovalDate(null);
        loan.setMonthlyEmi(null);
        loan.setInterestRate(null);
        loan.setLoanStartDate(null);
        loan.setLoanEndDate(null);

        LoanApplication updatedLoan = loanApplicationRepository.save(loan);
        return LoanApplicationResponse.fromEntity(updatedLoan);
    }

    // Helper method to map to response (ensure it handles new fields)
    private LoanApplicationResponse mapToResponse(LoanApplication loan) {
        // This method is correctly defined below the existing methods.
        // It's called by the other methods.
        // I moved it here for clarity within the full service class.
        return LoanApplicationResponse.builder()
                .id(loan.getId())
                .userId(loan.getUser() != null ? loan.getUser().getId() : null)
                .username(loan.getUser() != null ? loan.getUser().getUsername() : null)
                .loanAmount(loan.getLoanAmount())
                .loanType(loan.getLoanType())
                .durationMonths(loan.getDurationMonths())
                .purpose(loan.getPurpose())
                .annualIncome(loan.getAnnualIncome())
                .status(loan.getStatus())
                .applicationDate(loan.getApplicationDate())
                .approvalDate(loan.getApprovalDate())
                .monthlyEmi(loan.getMonthlyEmi())
                .interestRate(loan.getInterestRate())
                .loanStartDate(loan.getLoanStartDate())
                .loanEndDate(loan.getLoanEndDate())
                .officerNotes(loan.getOfficerNotes())
                .decisionDate(loan.getDecisionDate())
                .build();
    }


    // NEW HELPER METHOD: Calculate and set EMI, dates, etc.
    private void calculateAndSetLoanDetails(LoanApplication loan) {
        BigDecimal P = loan.getLoanAmount();
        BigDecimal R_annual = annualInterestRate; // Annual rate from properties
        int N = loan.getDurationMonths();

        // Monthly interest rate R = Annual Rate / 12
        BigDecimal R_monthly = R_annual.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        // EMI = P * R * (1 + R)^N / ((1 + R)^N - 1)
        // Using BigDecimal for precision
        BigDecimal ratePlusOneToPowerN = R_monthly.add(BigDecimal.ONE).pow(N);
        BigDecimal numerator = P.multiply(R_monthly).multiply(ratePlusOneToPowerN);
        BigDecimal denominator = ratePlusOneToPowerN.subtract(BigDecimal.ONE);

        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Cannot calculate EMI: Denominator is zero.");
        }

        BigDecimal monthlyEmi = numerator.divide(denominator, 2, RoundingMode.HALF_UP); // 2 decimal places for currency

        loan.setMonthlyEmi(monthlyEmi);
        loan.setInterestRate(R_annual); // Store annual rate for context
        loan.setLoanStartDate(LocalDateTime.now().plusDays(1)); // Loan starts tomorrow, for example
        loan.setLoanEndDate(loan.getLoanStartDate().plusMonths(loan.getDurationMonths()));
    }
}