package com.Loan.Loan_Management.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity // Marks this class as a JPA entity
@Table(name = "loan_applications") // Specifies the table name in the database
@Getter // Lombok: Generates all getter methods
@Setter // Lombok: Generates all setter methods
@NoArgsConstructor // Lombok: Generates a no-argument constructor
@AllArgsConstructor // Lombok: Generates a constructor with all fields
public class LoanApplication {

    @Id // Marks the field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increments the ID
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) // Lazy fetching for performance (load User only when needed)
    @JoinColumn(name = "user_id", nullable = false) // Foreign key column in loan_applications table
    private Users user; // The user who applied for the loan

    @Column(nullable = false)
    private BigDecimal loanAmount; // e.g., 50000.00

    @Column(nullable = false, length = 50)
    private String loanType; // e.g., "Personal Loan", "Home Loan", "Car Loan"

    @Column(nullable = false)
    private Integer durationMonths; // Loan tenure in months

    @Column(nullable = false, length = 255)
    private String purpose; // e.g., "Home renovation", "Education fees"

    @Column(nullable = false)
    private BigDecimal annualIncome; // Applicant's annual income

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING) // Stores the enum name as a string in the DB
    private LoanStatus status; // Enum for loan status (PENDING, APPROVED, REJECTED)

    @Column(nullable = false)
    private LocalDateTime applicationDate; // When the application was submitted

    // Fields added upon approval (these will be null initially)
    private LocalDateTime approvalDate;
    private BigDecimal monthlyEmi;
    private BigDecimal interestRate; // Annual interest rate, e.g., 0.10 for 10%
    private LocalDateTime loanStartDate;
    private LocalDateTime loanEndDate; // Calculated based on start date + duration

    // You might add a field for remarks by the loan officer upon approval/rejection
    @Column(length = 500)
    private String remarks;

    // Enum for Loan Status (define this inside or as a separate file)
    public enum LoanStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}