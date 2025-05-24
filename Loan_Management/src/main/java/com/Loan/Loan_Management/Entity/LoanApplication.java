package com.Loan.Loan_Management.Entity;
import com.Loan.Loan_Management.Entity.LoanStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder; // <--- NEW IMPORT for @Builder

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // <--- NEW ANNOTATION: Allows building objects with LoanApplication.builder().field(value).build()
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Foreign key column in loan_applications table
    private Users user; // The user who applied for the loan (renamed from 'customer' for consistency with 'user_id')

    @Column(nullable = false)
    private BigDecimal loanAmount;

    @Column(nullable = false, length = 50)
    private String loanType;

    @Column(nullable = false)
    private Integer durationMonths;

    @Column(nullable = false, length = 255)
    private String purpose;

    @Column(nullable = false)
    private BigDecimal annualIncome;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING) // Stores the enum name as a string in the DB
    private LoanStatus status; // <--- Now uses the external LoanStatus enum

    @Column(nullable = false)
    private LocalDateTime applicationDate;

    // Fields added for loan officer's decision and EMI calculation
    private LocalDateTime approvalDate;
    private BigDecimal monthlyEmi;
    private BigDecimal interestRate; // Annual interest rate, e.g., 0.10 for 10%
    private LocalDateTime loanStartDate;
    private LocalDateTime loanEndDate;

    @Column(name = "officer_notes", length = 500) // <--- NEW FIELD for officer's comments
    private String officerNotes;

    @Column(name = "decision_date") // <--- NEW FIELD for when decision was made
    private LocalDateTime decisionDate;
}