package com.Loan.Loan_Management.Repository;

import com.Loan.Loan_Management.Entity.LoanApplication;
import com.Loan.Loan_Management.Entity.LoanApplication.LoanStatus; // Import the enum
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    List<LoanApplication> findByUser_Id(Long userId);
    List<LoanApplication> findByStatus(LoanStatus status);
    List<LoanApplication> findByUser_IdAndStatus(Long userId, LoanStatus status);
    Optional<LoanApplication> findByIdAndUser_Id(Long id, Long userId);
}
