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

    @Value("${loan.interest-rate:0.12}")
    private BigDecimal annualInterestRate;

    @Transactional
    public LoanApplicationResponse applyForLoan(Long userId, LoanApplicationRequest request) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        LoanApplication loanApplication = LoanApplication.builder()
                .user(user)
                .loanAmount(request.getLoanAmount())
                .loanType(request.getLoanType())
                .durationMonths(request.getDurationMonths())
                .purpose(request.getPurpose())
                .annualIncome(request.getAnnualIncome())
                .status(LoanStatus.PENDING)
                .applicationDate(LocalDateTime.now())
                .build();

        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);
        return LoanApplicationResponse.fromEntity(savedApplication);
    }

    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getLoansByCustomerId(Long userId) {
        List<LoanApplication> applications = loanApplicationRepository.findByUser_Id(userId);
        return applications.stream()
                .map(LoanApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LoanApplicationResponse getLoanApplicationByIdAndCustomerId(Long loanId, Long userId) {
        LoanApplication loanApplication = loanApplicationRepository.findByIdAndUser_Id(loanId, userId)
                .orElseThrow(() -> new RuntimeException("Loan application not found or does not belong to user."));
        return LoanApplicationResponse.fromEntity(loanApplication);
    }

    @Transactional(readOnly = true)
    public LoanApplicationResponse getLoanApplicationById(Long loanId) {
        return loanApplicationRepository.findById(loanId)
                .map(LoanApplicationResponse::fromEntity)
                .orElseThrow(() -> new RuntimeException("Loan application not found with ID: " + loanId));
    }

    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getAllPendingApplications() {
        return loanApplicationRepository.findByStatus(LoanStatus.PENDING).stream()
                .map(LoanApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoanApplicationResponse approveLoan(Long loanId, LoanDecisionRequest request) {
        LoanApplication loan = loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan application not found with ID: " + loanId));

        if (loan.getStatus() != LoanStatus.PENDING && loan.getStatus() != LoanStatus.REJECTED) {
            throw new IllegalStateException("Loan application cannot be APPROVED from current status: " + loan.getStatus());
        }

        loan.setStatus(LoanStatus.APPROVED);
        loan.setOfficerNotes(request.getOfficerNotes());
        loan.setDecisionDate(LocalDateTime.now());
        loan.setApprovalDate(LocalDateTime.now());

        calculateAndSetLoanDetails(loan);

        LoanApplication updatedLoan = loanApplicationRepository.save(loan);
        return LoanApplicationResponse.fromEntity(updatedLoan);
    }

    @Transactional
    public LoanApplicationResponse rejectLoan(Long loanId, LoanDecisionRequest request) {
        LoanApplication loan = loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan application not found with ID: " + loanId));

        if (loan.getStatus() != LoanStatus.PENDING && loan.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException("Loan application cannot be REJECTED from current status: " + loan.getStatus());
        }

        loan.setStatus(LoanStatus.REJECTED);
        loan.setOfficerNotes(request.getOfficerNotes());
        loan.setDecisionDate(LocalDateTime.now());
        loan.setApprovalDate(null);
        loan.setMonthlyEmi(null);
        loan.setInterestRate(null);
        loan.setLoanStartDate(null);
        loan.setLoanEndDate(null);

        LoanApplication updatedLoan = loanApplicationRepository.save(loan);
        return LoanApplicationResponse.fromEntity(updatedLoan);
    }

    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getLoansByStatus(LoanStatus status) {
        List<LoanApplication> applications = loanApplicationRepository.findByStatus(status);
        return applications.stream()
                .map(LoanApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getAllLoanApplications() {
        List<LoanApplication> applications = loanApplicationRepository.findAll();
        return applications.stream()
                .map(LoanApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private void calculateAndSetLoanDetails(LoanApplication loan) {
        BigDecimal principal = loan.getLoanAmount();
        BigDecimal annualRate = annualInterestRate;
        int durationMonths = loan.getDurationMonths();

        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal ratePlusOneToPowerN = monthlyRate.add(BigDecimal.ONE).pow(durationMonths);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(ratePlusOneToPowerN);
        BigDecimal denominator = ratePlusOneToPowerN.subtract(BigDecimal.ONE);

        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Cannot calculate EMI: Denominator is zero (possibly due to zero duration).");
        }

        BigDecimal monthlyEmi = numerator.divide(denominator, 2, RoundingMode.HALF_UP);

        loan.setMonthlyEmi(monthlyEmi);
        loan.setInterestRate(annualRate);
        loan.setLoanStartDate(LocalDateTime.now());
        loan.setLoanEndDate(loan.getLoanStartDate().plusMonths(durationMonths));
    }
}