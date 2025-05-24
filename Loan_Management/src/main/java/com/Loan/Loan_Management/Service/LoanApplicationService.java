package com.Loan.Loan_Management.Service;

import com.Loan.Loan_Management.Entity.LoanApplication;
import com.Loan.Loan_Management.Entity.LoanStatus; // <--- IMPORT THE NEWLY SEPARATED ENUM
import com.Loan.Loan_Management.Entity.Users;
import com.Loan.Loan_Management.Repository.LoanApplicationRepository;
import com.Loan.Loan_Management.Repository.UserRepository;
import com.Loan.Loan_Management.dto.LoanApplicationRequest;
import com.Loan.Loan_Management.dto.LoanApplicationResponse;
import com.Loan.Loan_Management.dto.LoanDecisionRequest; // Make sure this is imported
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRepository userRepository;

    @Transactional
    public LoanApplicationResponse applyForLoan(LoanApplicationRequest request, Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        LoanApplication loanApplication = LoanApplication.builder() // <--- Using Builder
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

    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getLoanApplicationsByCustomer(Long userId) {
        List<LoanApplication> applications = loanApplicationRepository.findByUser_Id(userId);
        return applications.stream()
                .map(LoanApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LoanApplicationResponse getLoanApplicationByIdForCustomer(Long loanId, Long userId) {
        LoanApplication loanApplication = loanApplicationRepository.findByIdAndUser_Id(loanId, userId)
                .orElseThrow(() -> new RuntimeException("Loan application not found or does not belong to user."));
        return LoanApplicationResponse.fromEntity(loanApplication);
    }

    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getAllPendingApplications() {
        return loanApplicationRepository.findByStatus(LoanStatus.PENDING).stream()
                .map(LoanApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LoanApplicationResponse getLoanApplicationById(Long loanId) {
        return loanApplicationRepository.findById(loanId)
                .map(LoanApplicationResponse::fromEntity)
                .orElseThrow(() -> new RuntimeException("Loan application not found with ID: " + loanId));
    }

    // NEW: Approve Loan
    @Transactional
    public LoanApplicationResponse approveLoan(Long loanId, LoanDecisionRequest request) {
        LoanApplication loan = loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan application not found with ID: " + loanId));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException("Loan application is not in PENDING status. Current status: " + loan.getStatus());
        }

        loan.setStatus(LoanStatus.APPROVED);
        loan.setOfficerNotes(request.getNotes());
        loan.setDecisionDate(LocalDateTime.now());
        // You'll add EMI calculation and loan start/end dates here in the next step

        LoanApplication updatedLoan = loanApplicationRepository.save(loan);
        return LoanApplicationResponse.fromEntity(updatedLoan);
    }

    // NEW: Reject Loan
    @Transactional
    public LoanApplicationResponse rejectLoan(Long loanId, LoanDecisionRequest request) {
        LoanApplication loan = loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan application not found with ID: " + loanId));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException("Loan application is not in PENDING status. Current status: " + loan.getStatus());
        }

        loan.setStatus(LoanStatus.REJECTED);
        loan.setOfficerNotes(request.getNotes());
        loan.setDecisionDate(LocalDateTime.now());

        LoanApplication updatedLoan = loanApplicationRepository.save(loan);
        return LoanApplicationResponse.fromEntity(updatedLoan);
    }
}