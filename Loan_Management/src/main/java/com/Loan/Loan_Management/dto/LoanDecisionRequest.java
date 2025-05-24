package com.Loan.Loan_Management.dto;

import jakarta.validation.constraints.Size;
import lombok.Data; // For getters, setters, equals, hashCode, and toString

@Data // Lombok annotation to generate getters, setters, equals, hashCode, and toString
public class LoanDecisionRequest {

    // Officer's notes or reasons for the decision (approval or rejection)
    @Size(max = 500, message = "Notes cannot exceed 500 characters") // Validation constraint for length
    private String notes;
}