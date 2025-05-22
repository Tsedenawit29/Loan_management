package com.Loan.Loan_Management.controller;
import com.Loan.Loan_Management.dto.LoginRequest;
import com.Loan.Loan_Management.dto.LoginResponse;
import com.Loan.Loan_Management.Security.jwt.JwtService;
import com.Loan.Loan_Management.Security.Services.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomDetailService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateAndGetToken(@Valid @RequestBody LoginRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            if (authentication.isAuthenticated()) {
                // Load UserDetails explicitly if AuthenticationManager only returns a generic Authentication object
                UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
                String token = jwtService.generateToken(userDetails);
                return ResponseEntity.ok(new LoginResponse(token));
            } else {
                // This typically won't be reached as authenticate() throws an exception on failure
                return ResponseEntity.status(401).body(new LoginResponse("Authentication failed."));
            }
        } catch (AuthenticationException e) {
            // Handle bad credentials, etc.
            return ResponseEntity.status(401).body(new LoginResponse("Invalid username or password."));
        }
    }
}