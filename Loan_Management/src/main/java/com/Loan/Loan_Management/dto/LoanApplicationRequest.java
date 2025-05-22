package com.Loan.Loan_Management.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationRequest {

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "1000.00", message = "Loan amount must be at least 1000.00")
    private BigDecimal loanAmount;

    @NotBlank(message = "Loan type is required")
    private String loanType; // e.g., "Personal Loan", "Home Loan"

    @NotNull(message = "Duration in months is required")
    @Min(value = 6, message = "Loan duration must be at least 6 months")
    private Integer durationMonths;

    @NotBlank(message = "Purpose is required")
    private String purpose;

    @NotNull(message = "Annual income is required")
    @DecimalMin(value = "0.00", message = "Annual income cannot be negative")
    private BigDecimal annualIncome;
}