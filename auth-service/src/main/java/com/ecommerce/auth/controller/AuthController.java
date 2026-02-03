package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.*;
import com.ecommerce.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody PasswordChangeRequest request, Principal principal) {
        authService.changePassword(principal.getName(), request);
        return ResponseEntity.ok("Password updated successfully.");
    }

    @PutMapping("/admin-reset")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> adminChangePassword(@RequestBody AdminPasswordChange request) {
        authService.adminChangePassword(request);
        return ResponseEntity.ok("Password updated by admin");
    }
    @GetMapping("/admin/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UserResponse>> viewAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @DeleteMapping("/admin/delete")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> deleteUser (@Valid @RequestBody UserDeleteRequest request) {
        authService.deleteUser(request.email());
        return ResponseEntity.ok("User with the email " + request.email() + " has been deleted");
    }
}
