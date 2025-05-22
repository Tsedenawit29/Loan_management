package com.Loan.Loan_Management;

import com.Loan.Loan_Management.Entity.Roles;
import com.Loan.Loan_Management.Entity.Users; // <--- NEW IMPORT
import com.Loan.Loan_Management.Repository.RoleRepository;
import com.Loan.Loan_Management.Repository.UserRepository; // <--- NEW IMPORT
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder; // <--- NEW IMPORT

import java.time.LocalDateTime;
import java.util.Collections; // <--- NEW IMPORT
import java.util.HashSet;    // <--- NEW IMPORT

@SpringBootApplication
public class LoanManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoanManagementApplication.class, args);
	}

	// Renamed the bean method to reflect its new purpose: initializing roles AND default users
	@Bean
	public CommandLineRunner initRolesAndDefaultUsers(
			RoleRepository roleRepository,
			UserRepository userRepository,      // <--- INJECT UserRepository
			PasswordEncoder passwordEncoder     // <--- INJECT PasswordEncoder (for password hashing)
	) {
		return args -> {
			// --- 1. Initialize Roles (Existing logic, slightly refactored for clarity) ---
			Roles customerRole = roleRepository.findByRoleName("CUSTOMER")
					.orElseGet(() -> { // Use orElseGet to create and save only if absent
						Roles newRole = new Roles();
						newRole.setRoleName("CUSTOMER");
						roleRepository.save(newRole);
						System.out.println("CUSTOMER role initialized.");
						return newRole;
					});

			Roles loanOfficerRole = roleRepository.findByRoleName("LOAN_OFFICER")
					.orElseGet(() -> { // Use orElseGet to create and save only if absent
						Roles newRole = new Roles();
						newRole.setRoleName("LOAN_OFFICER");
						roleRepository.save(newRole);
						System.out.println("LOAN_OFFICER role initialized.");
						return newRole;
					});

			// --- 2. Initialize Default LOAN_OFFICER User ---
			// Check if a user with this username already exists to avoid duplicates on restart
			if (userRepository.findByUsername("default_officer").isEmpty()) {
				Users officerUser = new Users();
				officerUser.setUsername("default_officer");
				officerUser.setEmail("officer@example.com");
				// HASH THE PASSWORD! This is critical for security.
				officerUser.setPasswordHash(passwordEncoder.encode("officerpass")); // Choose a strong default password
				officerUser.setCreatedAt(LocalDateTime.now());
				officerUser.setUpdatedAt(LocalDateTime.now());
				// Assign the LOAN_OFFICER role
				officerUser.setRoles(new HashSet<>(Collections.singletonList(loanOfficerRole)));

				userRepository.save(officerUser);
				System.out.println("Default LOAN_OFFICER user 'default_officer' initialized.");
			}

			// --- 3. (Optional) Initialize a Default CUSTOMER User ---
			// This is useful for testing the customer journey without manual registration
			if (userRepository.findByUsername("default_customer").isEmpty()) {
				Users customerUser = new Users();
				customerUser.setUsername("default_customer");
				customerUser.setEmail("customer@default.com");
				customerUser.setPasswordHash(passwordEncoder.encode("customerpass")); // Hash the password
				customerUser.setCreatedAt(LocalDateTime.now());
				customerUser.setUpdatedAt(LocalDateTime.now());
				customerUser.setRoles(new HashSet<>(Collections.singletonList(customerRole))); // Assign CUSTOMER role

				userRepository.save(customerUser);
				System.out.println("Default CUSTOMER user 'default_customer' initialized.");
			}
		};
	}
}