package com.Loan.Loan_Management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationRequest {

    @NotBlank(message = "Username is required") // Updated message for clarity
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;

    @NotBlank(message = "Email is required") // Updated message for clarity
    @Email(message = "Invalid email format") // Updated message for clarity
    private String email;

    @NotBlank(message = "Password is required") // Updated message for clarity
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    // --- NEW FIELDS FOR REGISTRATION FORM ---
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @NotBlank(message = "Role is required")
    private String role; // e.g., "CUSTOMER" or "LOAN_OFFICER"

    // --- NEW HELPER METHOD FOR PASSWORD CONFIRMATION ---
    public boolean isPasswordConfirmed() {
        // Ensure neither is null to avoid NullPointerException, then compare
        return password != null && password.equals(confirmPassword);
    }
}