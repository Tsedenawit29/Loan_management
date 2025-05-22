package com.Loan.Loan_Management.controller;
import com.Loan.Loan_Management.dto.UserRegistrationRequest;
import com.Loan.Loan_Management.dto.UserResponse;
import com.Loan.Loan_Management.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // This endpoint is allowed to all, including unauthenticated users, as defined in SecurityConfig
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            UserResponse newUser = userService.registerUser(request);
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // This endpoint now requires authentication and the LOAN_OFFICER role
    @GetMapping
    @PreAuthorize("hasRole('LOAN_OFFICER')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // Example of allowing a user to retrieve their own data or a LOAN_OFFICER to retrieve any
    // This requires the principal in authentication to have the ID of the user.
    // For this, you might extend UserDetails in CustomUserDetailsService to include the entity ID.
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'CUSTOMER') and (authentication.principal.username == @userService.getUserById(#id).getUsername() or hasRole('LOAN_OFFICER'))")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        try {
            UserResponse user = userService.getUserById(id);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}
