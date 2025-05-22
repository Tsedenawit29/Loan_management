package com.Loan.Loan_Management.Repository;

import com.Loan.Loan_Management.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Users, Long> {
}
