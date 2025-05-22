package com.Loan.Loan_Management.Repository;

import com.Loan.Loan_Management.Entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Roles, Long> {
        Optional<Roles> findByRoleName(String roleName);
}
