package com.airesume.authservice.service;

import com.airesume.authservice.dto.*;
import com.airesume.authservice.model.*;
import com.airesume.authservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class handling core authentication, registration, and account recovery.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserQuotaRepository quotaRepository;

    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default Role not found"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .age(request.getAge())
                .mobileNumber(request.getMobileNumber())
                .roles(Collections.singleton(userRole))
                .provider(ProviderType.LOCAL)
                .isActive(true)
                .subscriptionPlan(PlanType.FREE)
                .build();

        userRepository.save(user);

        UserQuota quota = UserQuota.builder()
                .user(user)
                .aiCallsUsed(0)
                .atsChecksUsed(0)
                .build();
        quotaRepository.save(quota);

        return "User registered successfully";
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Username not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Incorrect Password");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
        String token = jwtService.generateToken(user.getUsername(), claims);

        // Send a welcome/thank-you login notification email (non-blocking)
        emailService.sendWelcomeLoginEmail(user.getEmail(), user.getFullName());

        return token;
    }

    public String initiateUsernameRecovery(UsernameRecoveryRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No account found with this email"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Incorrect password");
        }

        String otp = otpService.generateAndSaveOtp(user, VerificationOtp.OtpType.USERNAME_RECOVERY);
        emailService.sendOtpEmail(user.getEmail(), otp, "Username Recovery");
        return "Recovery OTP sent to your email";
    }

    public String verifyUsernameRecovery(OtpVerificationRequest request) {
        User user = userRepository.findByEmail(request.getIdentifier())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (otpService.validateOtp(user, request.getOtp(), VerificationOtp.OtpType.USERNAME_RECOVERY)) {
            emailService.sendUsernameEmail(user.getEmail(), user.getUsername());
            return "Username has been sent to your registered email";
        }
        throw new RuntimeException("Invalid or expired OTP");
    }

    /**
     * FIX: Now accepts a single `identifier` (email or username) via the
     * simplified PasswordResetInitiateRequest DTO, and calls
     * findByUsernameOrEmail() to find the user by either field.
     */
    public String initiatePasswordReset(PasswordResetInitiateRequest request) {
        String identifier = request.getIdentifier();
        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new RuntimeException("No account found with this email or username"));

        String otp = otpService.generateAndSaveOtp(user, VerificationOtp.OtpType.PASSWORD_RESET);
        emailService.sendOtpEmail(user.getEmail(), otp, "Password Reset");
        return "Password reset OTP sent to " + user.getEmail();
    }

    @Transactional
    public String resetPassword(OtpVerificationRequest request) {
        // FIX: `identifier` is an email (sent from frontend)
        User user = userRepository.findByEmail(request.getIdentifier())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (otpService.validateOtp(user, request.getOtp(), VerificationOtp.OtpType.PASSWORD_RESET)) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            return "Password reset successful";
        }
        throw new RuntimeException("Invalid or expired OTP");
    }

    @Transactional
    public String updateProfile(String username, ProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(request.getFullName());
        user.setMobileNumber(request.getMobileNumber());
        user.setAge(request.getAge());

        userRepository.save(user);
        return "Profile updated successfully";
    }

    @Transactional
    public String changePassword(String username, PasswordChangeRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Old password does not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return "Password changed successfully";
    }

    @Transactional
    public String updateSubscription(String username, PlanType plan) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setSubscriptionPlan(plan);
        userRepository.save(user);
        return "Subscription updated to " + plan;
    }

    @Transactional
    public String deactivateAccount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(false);
        userRepository.save(user);
        return "Account deactivated successfully";
    }

    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserProfileResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .mobileNumber(user.getMobileNumber())
                .age(user.getAge())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .subscriptionPlan(user.getSubscriptionPlan())
                .isActive(user.isActive())
                .build();
    }

    public String validateToken(String token) {
        if (jwtService.validateToken(token)) {
            return jwtService.extractUsername(token);
        }
        throw new RuntimeException("Invalid token");
    }

    public String refreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
        return jwtService.generateToken(username, claims);
    }

    // ── Admin Methods ─────────────────────────────────────────────────────────

    public List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserProfileResponse.builder()
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .mobileNumber(user.getMobileNumber())
                        .age(user.getAge())
                        .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                        .subscriptionPlan(user.getSubscriptionPlan())
                        .isActive(user.isActive())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public String updateUserStatus(String username, boolean active) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(active);
        userRepository.save(user);
        return "User status updated successfully";
    }

    @Transactional
    public String updateUserRole(String username, String roleName) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.getRoles().clear();
        user.getRoles().add(role);
        userRepository.save(user);
        return "User role updated successfully";
    }

    public List<UserProfileResponse> getUsersByRole(String roleName) {
        return userRepository.findAllByRoles_Name(roleName).stream()
                .map(user -> UserProfileResponse.builder()
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .mobileNumber(user.getMobileNumber())
                        .age(user.getAge())
                        .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                        .subscriptionPlan(user.getSubscriptionPlan())
                        .isActive(user.isActive())
                        .build())
                .collect(Collectors.toList());
    }

    public List<UserProfileResponse> getUsersByPlan(PlanType plan) {
        return userRepository.findBySubscriptionPlan(plan).stream()
                .map(user -> UserProfileResponse.builder()
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .mobileNumber(user.getMobileNumber())
                        .age(user.getAge())
                        .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                        .subscriptionPlan(user.getSubscriptionPlan())
                        .isActive(user.isActive())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public String deleteUserPermanently(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        quotaRepository.findByUserId(userId).ifPresent(quotaRepository::delete);
        userRepository.delete(user);
        return "User permanently deleted";
    }
}
