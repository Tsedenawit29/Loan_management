package com.Loan.Loan_Management.dto;

import com.Loan.Loan_Management.Entity.LoanApplication;
import com.Loan.Loan_Management.Entity.LoanStatus; // <--- IMPORT THE NEWLY SEPARATED ENUM
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder; // <--- NEW IMPORT for @Builder

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // <--- NEW ANNOTATION
public class LoanApplicationResponse {

    private Long id;
    private Long userId;
    private String username;
    private BigDecimal loanAmount;
    private String loanType;
    private Integer durationMonths;
    private String purpose;
    private BigDecimal annualIncome;
    private LoanStatus status; // <--- Now uses the external LoanStatus enum
    private LocalDateTime applicationDate;
    private LocalDateTime approvalDate;
    private BigDecimal monthlyEmi;
    private BigDecimal interestRate;
    private LocalDateTime loanStartDate;
    private LocalDateTime loanEndDate;
    private String officerNotes; // <--- NEW FIELD
    private LocalDateTime decisionDate; // <--- NEW FIELD

    // Static factory method to convert LoanApplication entity to DTO
    public static LoanApplicationResponse fromEntity(LoanApplication loanApplication) {
        if (loanApplication == null) {
            return null;
        }
        return LoanApplicationResponse.builder() // <--- Using builder now
                .id(loanApplication.getId())
                .userId(loanApplication.getUser() != null ? loanApplication.getUser().getId() : null)
                .username(loanApplication.getUser() != null ? loanApplication.getUser().getUsername() : null)
                .loanAmount(loanApplication.getLoanAmount())
                .loanType(loanApplication.getLoanType())
                .durationMonths(loanApplication.getDurationMonths())
                .purpose(loanApplication.getPurpose())
                .annualIncome(loanApplication.getAnnualIncome())
                .status(loanApplication.getStatus())
                .applicationDate(loanApplication.getApplicationDate())
                .approvalDate(loanApplication.getApprovalDate())
                .monthlyEmi(loanApplication.getMonthlyEmi())
                .interestRate(loanApplication.getInterestRate())
                .loanStartDate(loanApplication.getLoanStartDate())
                .loanEndDate(loanApplication.getLoanEndDate())
                .officerNotes(loanApplication.getOfficerNotes()) // <--- NEW FIELD
                .decisionDate(loanApplication.getDecisionDate()) // <--- NEW FIELD
                .build();
    }
}