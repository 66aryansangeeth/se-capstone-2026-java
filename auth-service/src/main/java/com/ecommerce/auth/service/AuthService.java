package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.*;
import com.ecommerce.auth.entity.AuditLog;
import com.ecommerce.auth.entity.Role;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.repository.AuditRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final AuditRepository auditRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if(userRepository.existsByEmail(request.email())){
            throw new RuntimeException("User with this email exists.");
        }
        var user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.valueOf(request.role().toUpperCase()))
                .build();

        userRepository.save(user);
        saveAuditLog(user.getEmail(), "REGISTERED", "Successfully created account");

        var token = jwtService.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .authorities(user.getRole().name())
                        .build()
        );
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        saveAuditLog(request.email(), "LOGIN", "Successful Login");

        var token = jwtService.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .authorities(user.getRole().name())
                        .build());
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
    public void changePassword(String email, PasswordChangeRequest request) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new RuntimeException("Incorrect password");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        saveAuditLog(email, "PASSWORD_CHANGE", "User has updated password");
    }

    public void adminChangePassword(AdminPasswordChange request) {
        var user = userRepository.findByEmail(request.email())
                .orElseThrow( () -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(request.NewPassword()));
        userRepository.save(user);

        saveAuditLog(request.email(), "ADM_PASSWORD_CHANGE", "Admin has changed the password");
    }
    public List<UserResponse> getAllUsers(){
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.getCreatedAt()
                ))
                .toList();
    }
    public void deleteUser(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);

        saveAuditLog(email, "DELETED", "User deleted by admin");
    }

    private void saveAuditLog(String userEmail, String action, String details) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String adminEmail = (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal()))
                ? auth.getName()
                : "SYSTEM";
        AuditLog log = AuditLog.builder()
                .adminEmail(adminEmail)
                .userEmail(userEmail)
                .action(action)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
        auditRepository.save(log);
    }


}
