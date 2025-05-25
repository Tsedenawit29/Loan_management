package com.Loan.Loan_Management.dto;

import jakarta.validation.constraints.Size;
import lombok.Data; // For getters, setters, equals, hashCode, and toString
import lombok.NoArgsConstructor; // Add this if not already present, good practice for DTOs
import lombok.AllArgsConstructor; // Add this if not already present, good practice for DTOs

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanDecisionRequest {

    // Officer's notes or reasons for the decision (approval or rejection)
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String officerNotes;
}