package com.Loan.Loan_Management.controller;

import com.Loan.Loan_Management.Service.UserService;
import com.Loan.Loan_Management.dto.UserRegistrationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class RegistrationController {

    private final UserService userService; // <--- CHANGE THIS INJECTION from AuthService

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userRegistrationRequest", new UserRegistrationRequest());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userRegistrationRequest") UserRegistrationRequest request,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (!request.isPasswordConfirmed()) {
            bindingResult.rejectValue("confirmPassword", "error.userRegistrationRequest", "Passwords do not match");
            return "register";
        }

        try {
            // Call your UserService to register the user
            userService.registerUser(request); // <--- Use userService.registerUser
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) { // Catch specific exceptions from UserService
            model.addAttribute("error", e.getMessage());
            return "register";
        } catch (RuntimeException e) { // Catch any other unexpected runtime exceptions
            model.addAttribute("error", "An unexpected error occurred during registration.");
            return "register";
        }
    }
}