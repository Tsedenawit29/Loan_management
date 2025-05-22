package com.Loan.Loan_Management.dto;

import com.Loan.Loan_Management.Entity.LoanApplication;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationResponse {

    private Long id;
    private Long userId; // The ID of the user who applied
    private String username; // The username of the applicant
    private BigDecimal loanAmount;
    private String loanType;
    private Integer durationMonths;
    private String purpose;
    private BigDecimal annualIncome;
    private String status; // String representation of LoanStatus
    private LocalDateTime applicationDate;
    private LocalDateTime approvalDate;
    private BigDecimal monthlyEmi;
    private BigDecimal interestRate;
    private LocalDateTime loanStartDate;
    private LocalDateTime loanEndDate;
    private String remarks;

    // Static factory method to convert LoanApplication entity to DTO
    public static LoanApplicationResponse fromEntity(LoanApplication loanApplication) {
        if (loanApplication == null) {
            return null;
        }
        LoanApplicationResponse response = new LoanApplicationResponse();
        response.setId(loanApplication.getId());
        response.setUserId(loanApplication.getUser().getId()); // Get user ID from associated user
        response.setUsername(loanApplication.getUser().getUsername()); // Get username from associated user
        response.setLoanAmount(loanApplication.getLoanAmount());
        response.setLoanType(loanApplication.getLoanType());
        response.setDurationMonths(loanApplication.getDurationMonths());
        response.setPurpose(loanApplication.getPurpose());
        response.setAnnualIncome(loanApplication.getAnnualIncome());
        response.setStatus(loanApplication.getStatus().name()); // Convert enum to String
        response.setApplicationDate(loanApplication.getApplicationDate());
        response.setApprovalDate(loanApplication.getApprovalDate());
        response.setMonthlyEmi(loanApplication.getMonthlyEmi());
        response.setInterestRate(loanApplication.getInterestRate());
        response.setLoanStartDate(loanApplication.getLoanStartDate());
        response.setLoanEndDate(loanApplication.getLoanEndDate());
        response.setRemarks(loanApplication.getRemarks());
        return response;
    }
}