package com.Loan.Loan_Management.Service;
import com.Loan.Loan_Management.Entity.Roles;
import com.Loan.Loan_Management.Entity.Users;
import com.Loan.Loan_Management.dto.UserRegistrationRequest;
import com.Loan.Loan_Management.dto.UserResponse;
import com.Loan.Loan_Management.Repository.RoleRepository;
import com.Loan.Loan_Management.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Lombok for constructor injection
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder; // Injected BCryptPasswordEncoder

    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already taken: " + request.getUsername());
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        Users user = new Users();
        // ID is auto-generated, no need to set here
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); // Hash the password!

        Roles customerRole = roleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Error: Role 'CUSTOMER' not found. Please initialize roles."));
        user.addRole(customerRole);

        Users savedUser = userRepository.save(user);

        return new UserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::new)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }
}