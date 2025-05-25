package com.Loan.Loan_Management.dto;
import com.Loan.Loan_Management.Entity.Users;
import lombok.Data;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Set<String> roles;

    public UserResponse(Users user) {
        this.id = Long.valueOf(user.getId());
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.roles = user.getRoles().stream()
                .map(role -> role.getRoleName())
                .collect(Collectors.toSet());
    }
}
