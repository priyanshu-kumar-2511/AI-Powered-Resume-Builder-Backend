
package com.airesume.authservice.service;

import com.airesume.authservice.dto.*;
import com.airesume.authservice.model.*;
import com.airesume.authservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core Authentication and Account Management Service.
 * Orchestrates user registration with OTP verification, JWT issuance,
 * password recovery, and subscription plan synchronization across services.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    // Repositories for data persistence
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OtpRepository otpRepository;

    // Core services for logic
    private final OtpService otpService;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserQuotaRepository quotaRepository;

    /**
     * Step 1 of User Registration.
     * Checks for existing users, creates an inactive record, and sends
     * a 6-digit verification OTP to the user's email.
     * 
     * @param request the registration details provided by the user
     * @return a success message
     * @throws RuntimeException if username or email already exists
     */
    @Transactional
    public String initiateRegistration(RegisterInitiateRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            if (user.isActive()) {
                throw new RuntimeException("Email is already registered");
            } else {
                // Clean up any existing inactive session
                quotaRepository.findByUserId(user.getId()).ifPresent(quotaRepository::delete);
                otpRepository.deleteByUser(user);
                userRepository.delete(user);
                userRepository.flush();
            }
        });

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default Role not found"));

        User user = User.builder()
                .username("temp_" + UUID.randomUUID().toString().substring(0, 8))
                .email(request.getEmail())
                .password("pending_verification")
                .fullName(request.getFullName())
                .age(request.getAge())
                .mobileNumber(request.getMobileNumber())
                .roles(Collections.singleton(userRole))
                .provider(ProviderType.LOCAL)
                .isActive(false)
                .subscriptionPlan(PlanType.FREE)
                .build();

        userRepository.save(user);

        String otp = otpService.generateAndSaveOtp(user, VerificationOtp.OtpType.REGISTRATION);
        emailService.sendOtpEmail(user.getEmail(), otp, "User Registration Verification");

        return "Verification OTP sent to your email";
    }

    /**
     * Step 2 of User Registration.
     * Validates the provided OTP for the given email session.
     */
    public String verifyRegistrationOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Registration session not found"));

        if (user.isActive()) {
            throw new RuntimeException("Email is already registered and verified");
        }

        if (otpService.validateOtp(user, otp, VerificationOtp.OtpType.REGISTRATION)) {
            return "OTP verified successfully";
        }
        throw new RuntimeException("Invalid or expired OTP");
    }

    /**
     * Completes user registration and sets credentials (Step 3).
     */
    @Transactional
    public String register(RegisterRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(
                        () -> new RuntimeException("Registration session not found. Please complete Step 1 first."));

        if (user.isActive()) {
            throw new RuntimeException("Email is already registered and verified");
        }

        if (!otpService.validateOtp(user, request.getOtp(), VerificationOtp.OtpType.REGISTRATION)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        user.setEnabled(true);
        userRepository.save(user);

        // Delete the registration OTP after success
        otpRepository.deleteByUser(user);

        UserQuota quota = UserQuota.builder()
                .user(user)
                .aiCallsUsed(0)
                .atsChecksUsed(0)
                .build();
        quotaRepository.save(quota);

        return "User registered successfully";
    }

    /**
     * Authenticates a user based on their credentials.
     * Checks password validity and account status. If successful, generates a JWT
     * and sends a welcome or admin alert email.
     * 
     * @param request the login credentials
     * @return a valid JWT token
     * @throws RuntimeException if user not found, incorrect password, or account is
     *                          suspended
     */
    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Username not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Incorrect Password");
        }

        if (!user.isActive()) {
            throw new RuntimeException("ACCOUNT_SUSPENDED");
        }

        String token = jwtService.generateToken(user.getUsername(), buildAuthClaims(user));

        // Send notification email (non-blocking)
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        if (isAdmin) {
            emailService.sendAdminLoginAlertEmail(user.getEmail(), user.getFullName());
        } else {
            emailService.sendWelcomeLoginEmail(user.getEmail(), user.getFullName());
        }

        return token;
    }


    /**
     * Initiates the username recovery flow by generating and emailing an OTP.
     * 
     * @param request the email and password of the account
     * @return a confirmation message that the OTP was sent
     */
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

    /**
     * Verifies the provided OTP for username recovery. If valid, emails the
     * username to the user.
     * 
     * @param request the email and OTP
     * @return a success message
     */
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

    /**
     * Completes the password reset process by verifying the OTP and saving the new
     * password.
     * 
     * @param request the identifier (email), OTP, and new password
     * @return a success message
     */
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

    /**
     * Updates the basic profile information of an existing user.
     * 
     * @param username the username of the user
     * @param request  the updated profile fields
     * @return a success message
     */
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

    /**
     * Changes a user's password if the current password provided is correct.
     * 
     * @param username the username of the user
     * @param request  the old and new passwords
     * @return a success message
     */
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

    /**
     * Updates the subscription plan for a specific user.
     * 
     * @param username the username of the user
     * @param plan     the new subscription plan
     * @return a success message
     */
    @Transactional
    public String updateSubscription(String username, PlanType plan) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setSubscriptionPlan(plan);
        userRepository.save(user);
        return "Subscription updated to " + plan;
    }

    /**
     * Deactivates a user's account. This prevents them from logging in.
     * 
     * @param username the username of the user to deactivate
     * @return a success message
     */
    @Transactional
    public String deactivateAccount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(false);
        userRepository.save(user);
        return "Account deactivated successfully";
    }

    /**
     * Retrieves the profile information for a specific user.
     * 
     * @param username the username of the user
     * @return a DTO containing the user's profile details
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return toUserProfileResponse(user);
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

        return jwtService.generateToken(username, buildAuthClaims(user));
    }

    // ── Admin Methods ─────────────────────────────────────────────────────────

    /**
     * Retrieves a list of all users. Typically used by Admins.
     * 
     * @return a list of user profiles
     */
    @Transactional(readOnly = true)
    public List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserProfileResponse)
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

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getUsersByRole(String roleName) {
        return userRepository.findAllByRoles_Name(roleName).stream()
                .map(this::toUserProfileResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getUsersByPlan(PlanType plan) {
        return userRepository.findBySubscriptionPlan(plan).stream()
                .map(this::toUserProfileResponse)
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

    @Transactional
    public String deleteOwnAccount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        quotaRepository.findByUserId(user.getId()).ifPresent(quotaRepository::delete);
        userRepository.delete(user);
        return "Account permanently deleted successfully";
    }

    private Map<String, Object> buildAuthClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
        claims.put("userId", user.getId());
        claims.put("subscriptionPlan", user.getSubscriptionPlan().name());
        return claims;
    }

    private UserProfileResponse toUserProfileResponse(User user) {
        return UserProfileResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .mobileNumber(user.getMobileNumber())
                .age(user.getAge())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .subscriptionPlan(user.getSubscriptionPlan())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .premiumExpiresAt(user.getPremiumExpiresAt())
                .build();
    }

    // ── Admin: userId-based methods ───────────────────────────────────────────

    /**
     * Suspends a user by their ID, preventing them from logging in, and sends an
     * email notification.
     * 
     * @param userId the ID of the user
     * @param reason the reason for suspension provided by the admin
     * @return a success message
     */
    @Transactional
    public String suspendUserById(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setActive(false);
        userRepository.save(user);
        emailService.sendSuspensionEmail(user.getEmail(), user.getFullName(), reason);
        return "User " + user.getUsername() + " suspended.";
    }

    /**
     * Reactivates a suspended user and sends them an email notification.
     * 
     * @param userId the ID of the user
     * @return a success message
     */
    @Transactional
    public String reactivateUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setActive(true);
        userRepository.save(user);
        emailService.sendReactivationEmail(user.getEmail(), user.getFullName());
        return "User " + user.getUsername() + " reactivated.";
    }

    /**
     * Updates a user's subscription plan by their username.
     * Triggers email notifications for Premium activation/cancellation.
     */
    @Transactional
    public String updateSubscriptionByUsername(String username, PlanType plan) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        user.setSubscriptionPlan(plan);
        userRepository.save(user);

        if (plan == PlanType.PREMIUM) {
            emailService.sendPremiumActivationEmail(user.getEmail(), user.getFullName());
        } else if (plan == PlanType.FREE) {
            emailService.sendPremiumCancellationEmail(user.getEmail(), user.getFullName());
        }

        return "User " + username + " plan updated to " + (plan != null ? plan.name() : "NONE");
    }

    /**
     * Updates a user's subscription plan by their unique ID.
     * Automatically calculates premium expiry (30 days) if upgraded.
     * 
     * @param userId the ID of the user
     * @param plan   the new subscription plan
     * @return a success message
     */
    @Transactional
    public String updateSubscriptionById(Long userId, PlanType plan) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setSubscriptionPlan(plan);
        if (plan == PlanType.PREMIUM) {
            user.setPremiumExpiresAt(LocalDateTime.now().plusDays(30));
            emailService.sendPremiumActivationEmail(user.getEmail(), user.getFullName());
        } else {
            user.setPremiumExpiresAt(null);
        }
        userRepository.save(user);

        return "User " + user.getUsername() + " plan updated to " + plan.name();
    }

    /**
     * Updates a user's role by their ID. If promoted to ADMIN, sends an email
     * notification.
     * 
     * @param userId   the ID of the user
     * @param roleName the new role
     * @return a success message
     */
    @Transactional
    public String updateUserRoleById(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        user.getRoles().clear();
        user.getRoles().add(role);
        userRepository.save(user);

        if ("ROLE_ADMIN".equals(roleName)) {
            emailService.sendAdminPromotionEmail(user.getEmail(), user.getFullName());
        } else if ("ROLE_USER".equals(roleName)) {
            emailService.sendAdminDemotionEmail(user.getEmail(), user.getFullName());
        }

        return "User " + user.getUsername() + " role updated to " + roleName;
    }

    /**
     * Returns a synthetic audit log list built from the current user table.
     * In a production system this would read from a dedicated audit_log table.
     * Each entry records account creation or last-known status change.
     */
    public List<Map<String, Object>> getAuditLogs() {
        return userRepository.findAll().stream()
                .map(u -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("logId", u.getId());
                    entry.put("actorId", u.getId());
                    entry.put("actorEmail", u.getEmail());
                    entry.put("actorName", u.getFullName());
                    entry.put("actionType", u.isActive() ? "USER_REGISTERED" : "USER_SUSPENDED");
                    entry.put("entityType", "USER");
                    entry.put("entityId", String.valueOf(u.getId()));
                    entry.put("beforeState", null);
                    entry.put("afterState", null);
                    entry.put("ipAddress", "—");
                    entry.put("timestamp", u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
                    return entry;
                })
                .collect(Collectors.toList());
    }
}
