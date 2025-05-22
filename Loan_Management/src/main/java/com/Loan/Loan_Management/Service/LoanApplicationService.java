package com.Loan.Loan_Management.Service;

import com.Loan.Loan_Management.Entity.LoanApplication;
import com.Loan.Loan_Management.Entity.LoanApplication.LoanStatus;
import com.Loan.Loan_Management.Entity.Users; // Import Users entity
import com.Loan.Loan_Management.Repository.LoanApplicationRepository;
import com.Loan.Loan_Management.Repository.UserRepository; // Import UserRepository
import com.Loan.Loan_Management.dto.LoanApplicationRequest;
import com.Loan.Loan_Management.dto.LoanApplicationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import for transactional methods

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service // Marks this class as a Spring service
@RequiredArgsConstructor // Lombok: Generates constructor for final fields
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRepository userRepository; // To fetch the User entity for the application

    @Transactional // Ensures the method runs in a transaction
    public LoanApplicationResponse applyForLoan(LoanApplicationRequest request, Long userId) {
        // 1. Fetch the User entity
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // 2. Create LoanApplication entity from DTO
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setUser(user); // Set the user who applied
        loanApplication.setLoanAmount(request.getLoanAmount());
        loanApplication.setLoanType(request.getLoanType());
        loanApplication.setDurationMonths(request.getDurationMonths());
        loanApplication.setPurpose(request.getPurpose());
        loanApplication.setAnnualIncome(request.getAnnualIncome());
        loanApplication.setStatus(LoanStatus.PENDING); // Initial status
        loanApplication.setApplicationDate(LocalDateTime.now()); // Set application timestamp

        // 3. Save the loan application
        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);

        // 4. Convert and return Response DTO
        return LoanApplicationResponse.fromEntity(savedApplication);
    }

    @Transactional(readOnly = true) // Read-only transaction for fetching data
    public List<LoanApplicationResponse> getLoanApplicationsByCustomer(Long userId) {
        List<LoanApplication> applications = loanApplicationRepository.findByUser_Id(userId);
        // Convert list of entities to list of DTOs
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

    // --- LOAN OFFICER Specific Methods (will be implemented in next module) ---
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

    // Placeholder for future approval/rejection logic
    // @Transactional
    // public LoanApplicationResponse approveLoan(Long loanId, String remarks, BigDecimal interestRate) { /* ... */ }
    // @Transactional
    // public LoanApplicationResponse rejectLoan(Long loanId, String remarks) { /* ... */ }
}