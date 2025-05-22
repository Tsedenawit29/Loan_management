package com.Loan.Loan_Management.config.security; // Recommended package

import com.Loan.Loan_Management.Entity.Users; // Import your Users entity
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

// This class WRAPS your Users entity and implements Spring Security's UserDetails
public class CustomUserDetail implements UserDetails {

    private Long id; // <--- This is the crucial field to store your database ID!
    private String username;
    private String password;
    private List<GrantedAuthority> authorities;

    // Constructor to build CustomUserDetails from your Users entity
    public CustomUserDetail(Users user) {
        this.id = user.getId(); // Get the ID from your database entity
        this.username = user.getUsername();
        this.password = user.getPasswordHash(); // Assuming passwordHash is the correct field for hashed password
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .collect(Collectors.toList());
    }

    // <--- IMPORTANT: Add this getter for the ID
    public Long getId() {
        return id;
    }

    // --- All other UserDetails methods (Spring Security requires these) ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // You can set these to 'true' by default for now unless you implement account locking/expiration
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}
