package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.*;
import com.ecommerce.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PutMapping("/change-password")
    @ResponseStatus(HttpStatus.OK)
    public String changePassword(@RequestBody PasswordChangeRequest request, Principal principal) {
        authService.changePassword(principal.getName(), request);
        return "Password updated successfully.";
    }

    @PutMapping("/admin-reset")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public String adminChangePassword(@RequestBody AdminPasswordChange request) {
        authService.adminChangePassword(request);
        return "Password updated by admin";
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponse> viewAllUsers() {
        return authService.getAllUsers();
    }

    @DeleteMapping("/admin/delete")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public String deleteUser(@Valid @RequestBody UserDeleteRequest request) {
        authService.deleteUser(request.email());
        return "User with the email " + request.email() + " has been deleted";
    }
}
