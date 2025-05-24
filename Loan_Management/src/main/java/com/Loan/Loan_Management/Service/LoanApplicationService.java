package com.Loan.Loan_Management.Service;

import com.Loan.Loan_Management.Entity.LoanApplication;
import com.Loan.Loan_Management.Entity.LoanStatus;
import com.Loan.Loan_Management.Entity.Users;
import com.Loan.Loan_Management.Repository.LoanApplicationRepository;
import com.Loan.Loan_Management.Repository.UserRepository;
import com.Loan.Loan_Management.dto.LoanApplicationRequest;
import com.Loan.Loan_Management.dto.LoanApplicationResponse;
import com.Loan.Loan_Management.dto.LoanDecisionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    /**
     * Handles the submission of a new loan application by a user.
     * @param userId The ID of the user submitting the application.
     * @param request The DTO containing loan application details.
     * @return LoanApplicationResponse of the created application.
     */
    @Transactional
    public LoanApplicationResponse applyForLoan(Long userId, LoanApplicationRequest request) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Using Lombok's @Builder here for cleaner entity creation, given your LoanApplication has @Builder
        LoanApplication loanApplication = LoanApplication.builder()
                .user(user)
                .loanAmount(request.getLoanAmount()) // Field matches DTO and entity
                .loanType(request.getLoanType())
                .durationMonths(request.getDurationMonths())
                .purpose(request.getPurpose())
                .annualIncome(request.getAnnualIncome())
                .status(LoanStatus.PENDING) // Initial status
                .applicationDate(LocalDateTime.now()) // Set application date
                .build();

        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);
        return LoanApplicationResponse.fromEntity(savedApplication);
    }

    /**
     * Retrieves all loan applications for a specific customer.
     * @param userId The ID of the customer.
     * @return A list of LoanApplicationResponse DTOs.
     */
    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getLoansByCustomerId(Long userId) {
        // Assuming findByUser_Id maps to LoanApplication.user.id
        List<LoanApplication> applications = loanApplicationRepository.findByUser_Id(userId);
        return applications.stream()
                .map(LoanApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single loan application by ID for a specific customer, ensuring ownership.
     * @param loanId The ID of the loan application.
     * @param userId The ID of the customer.
     * @return LoanApplicationResponse DTO.
     */
    @Transactional(readOnly = true)
    public LoanApplicationResponse getLoanApplicationByIdForCustomer(Long loanId, Long userId) {
        LoanApplication loanApplication = loanApplicationRepository.findByIdAndUser_Id(loanId, userId)
                .orElseThrow(() -> new RuntimeException("Loan application not found or does not belong to user."));
        return LoanApplicationResponse.fromEntity(loanApplication);
    }

    /**
     * Retrieves a single loan application by ID (typically for internal/officer use).
     * @param loanId The ID of the loan application.
     * @return LoanApplicationResponse DTO.
     */
    @Transactional(readOnly = true)
    public LoanApplicationResponse getLoanApplicationById(Long loanId) {
        return loanApplicationRepository.findById(loanId)
                .map(LoanApplicationResponse::fromEntity)
                .orElseThrow(() -> new RuntimeException("Loan application not found with ID: " + loanId));
    }

    /**
     * Retrieves all pending loan applications (typically for loan officers).
     * @return A list of LoanApplicationResponse DTOs.
     */
    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getAllPendingApplications() {
        return loanApplicationRepository.findByStatus(LoanStatus.PENDING).stream()
                .map(LoanApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Approves a loan application and calculates loan details.
     * @param loanId The ID of the loan to approve.
     * @param request Decision details including notes.
     * @return LoanApplicationResponse of the updated loan.
     */
    @Transactional
    public LoanApplicationResponse approveLoan(Long loanId, LoanDecisionRequest request) {
        LoanApplication loan = loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan application not found with ID: " + loanId));

        if (loan.getStatus() != LoanStatus.PENDING && loan.getStatus() != LoanStatus.REJECTED) {
            throw new IllegalStateException("Loan application cannot be APPROVED from current status: " + loan.getStatus());
        }

        loan.setStatus(LoanStatus.APPROVED);
        loan.setOfficerNotes(request.getNotes());
        loan.setDecisionDate(LocalDateTime.now());
        loan.setApprovalDate(LocalDateTime.now());

        calculateAndSetLoanDetails(loan); // Calculate EMI, dates etc.

        LoanApplication updatedLoan = loanApplicationRepository.save(loan);
        return LoanApplicationResponse.fromEntity(updatedLoan);
    }

    /**
     * Rejects a loan application.
     * @param loanId The ID of the loan to reject.
     * @param request Decision details including notes.
     * @return LoanApplicationResponse of the updated loan.
     */
    @Transactional
    public LoanApplicationResponse rejectLoan(Long loanId, LoanDecisionRequest request) {
        LoanApplication loan = loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan application not found with ID: " + loanId));

        if (loan.getStatus() != LoanStatus.PENDING && loan.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException("Loan application cannot be REJECTED from current status: " + loan.getStatus());
        }

        loan.setStatus(LoanStatus.REJECTED);
        loan.setOfficerNotes(request.getNotes());
        loan.setDecisionDate(LocalDateTime.now());
        // Reset approval-related fields on rejection
        loan.setApprovalDate(null);
        loan.setMonthlyEmi(null);
        loan.setInterestRate(null);
        loan.setLoanStartDate(null);
        loan.setLoanEndDate(null);

        LoanApplication updatedLoan = loanApplicationRepository.save(loan);
        return LoanApplicationResponse.fromEntity(updatedLoan);
    }

    // Helper method to calculate EMI, loan start/end dates, and set them on the LoanApplication entity
    private void calculateAndSetLoanDetails(LoanApplication loan) {
        BigDecimal principal = loan.getLoanAmount(); // <--- CORRECTED: Using getLoanAmount()
        BigDecimal annualRate = annualInterestRate;
        int durationMonths = loan.getDurationMonths();

        // Monthly interest rate
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        // EMI = P * R * (1 + R)^N / ((1 + R)^N - 1)
        BigDecimal ratePlusOneToPowerN = monthlyRate.add(BigDecimal.ONE).pow(durationMonths);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(ratePlusOneToPowerN);
        BigDecimal denominator = ratePlusOneToPowerN.subtract(BigDecimal.ONE);

        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Cannot calculate EMI: Denominator is zero.");
        }

        BigDecimal monthlyEmi = numerator.divide(denominator, 2, RoundingMode.HALF_UP); // Round to 2 decimal places

        loan.setMonthlyEmi(monthlyEmi);
        loan.setInterestRate(annualRate); // Store annual rate for context
        loan.setLoanStartDate(LocalDateTime.now()); // Loan starts now upon approval
        loan.setLoanEndDate(loan.getLoanStartDate().plusMonths(durationMonths));
    }
}