package com.Loan.Loan_Management.dto;

import com.Loan.Loan_Management.Entity.LoanApplication;
import com.Loan.Loan_Management.Entity.LoanStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanApplicationResponse {
    private Long id;
    private Long userId;
    private String username;
    private BigDecimal loanAmount; // Field name here matches DTO and entity
    private String loanType;
    private Integer durationMonths;
    private String purpose;
    private BigDecimal annualIncome;
    private LoanStatus status;
    private LocalDateTime applicationDate;
    private LocalDateTime approvalDate;
    private BigDecimal monthlyEmi;
    private BigDecimal interestRate;
    private LocalDateTime loanStartDate;
    private LocalDateTime loanEndDate;
    private String officerNotes;
    private LocalDateTime decisionDate;

    // Static method to convert LoanApplication entity to LoanApplicationResponse DTO
    public static LoanApplicationResponse fromEntity(LoanApplication entity) {
        if (entity == null) {
            return null;
        }
        return LoanApplicationResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .username(entity.getUser() != null ? entity.getUser().getUsername() : null)
                .loanAmount(entity.getLoanAmount()) // <--- CORRECTED: Using getLoanAmount()
                .loanType(entity.getLoanType())
                .durationMonths(entity.getDurationMonths())
                .purpose(entity.getPurpose())
                .annualIncome(entity.getAnnualIncome())
                .status(entity.getStatus())
                .applicationDate(entity.getApplicationDate())
                .approvalDate(entity.getApprovalDate())
                .monthlyEmi(entity.getMonthlyEmi())
                .interestRate(entity.getInterestRate())
                .loanStartDate(entity.getLoanStartDate())
                .loanEndDate(entity.getLoanEndDate())
                .officerNotes(entity.getOfficerNotes())
                .decisionDate(entity.getDecisionDate())
                .build();
    }
}