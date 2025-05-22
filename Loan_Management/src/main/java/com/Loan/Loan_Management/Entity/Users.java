package com.Loan.Loan_Management.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash; // Store hashed passwords!

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // Set default value

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now(); // Set default value
    @ManyToMany(fetch = FetchType.EAGER) // Fetch roles eagerly when user is loaded
    @JoinTable(
            name = "user_roles", // Name of the join table
            joinColumns = @JoinColumn(name = "user_id"), // Column in user_roles that refers to user
            inverseJoinColumns = @JoinColumn(name = "role_id") // Column in user_roles that refers to role
    )
    private Set<Roles> roles = new HashSet<>();
    // Add methods to manage roles more easily
    public void addRole(Roles role) {
        this.roles.add(role);
    }

    public void removeRole(Roles role) {
        this.roles.remove(role);
    }
}
