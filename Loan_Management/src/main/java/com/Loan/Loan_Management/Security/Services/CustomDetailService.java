package com.Loan.Loan_Management.Security.Services;

import com.Loan.Loan_Management.Entity.Users;
import com.Loan.Loan_Management.Repository.UserRepository;
import com.Loan.Loan_Management.config.security.CustomUserDetail; // <--- IMPORT YOUR NEW CustomUserDetails CLASS
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// You no longer need `java.util.stream.Collectors` here if CustomUserDetails handles mapping roles

@Service
public class CustomDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // <--- NOW RETURN AN INSTANCE OF YOUR CustomUserDetails
        return new CustomUserDetail(user);
    }
}