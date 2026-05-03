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

    /**
     * Registers a new user with the default ROLE_USER and FREE subscription plan.
     * Also initializes their default AI quotas.
     * @param request the registration details provided by the user
     * @return a success message
     * @throws RuntimeException if username or email already exists
     */
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

    /**
     * Authenticates a user based on their credentials.
     * Checks password validity and account status. If successful, generates a JWT
     * and sends a welcome or admin alert email.
     * @param request the login credentials
     * @return a valid JWT token
     * @throws RuntimeException if user not found, incorrect password, or account is suspended
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
     * Verifies the provided OTP for username recovery. If valid, emails the username to the user.
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
     * Completes the password reset process by verifying the OTP and saving the new password.
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
     * @param username the username of the user
     * @param request the updated profile fields
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
     * @param username the username of the user
     * @param request the old and new passwords
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
     * @param username the username of the user
     * @param plan the new subscription plan
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
     * @param username the username of the user
     * @return a DTO containing the user's profile details
     */
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
     * @return a list of user profiles
     */
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

    public List<UserProfileResponse> getUsersByRole(String roleName) {
        return userRepository.findAllByRoles_Name(roleName).stream()
                .map(this::toUserProfileResponse)
                .collect(Collectors.toList());
    }

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
                .build();
    }

    // ── Admin: userId-based methods ───────────────────────────────────────────

    /**
     * Suspends a user by their ID, preventing them from logging in, and sends an email notification.
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
     * Updates a user's subscription plan by their ID. If upgraded to PREMIUM, sends an email notification.
     * @param userId the ID of the user
     * @param plan the new subscription plan
     * @return a success message
     */
    @Transactional
    public String updateSubscriptionById(Long userId, PlanType plan) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setSubscriptionPlan(plan);
        userRepository.save(user);

        if (plan == PlanType.PREMIUM) {
            emailService.sendPremiumActivationEmail(user.getEmail(), user.getFullName());
        }
        
        return "User " + user.getUsername() + " plan updated to " + plan.name();
    }

    /**
     * Updates a user's role by their ID. If promoted to ADMIN, sends an email notification.
     * @param userId the ID of the user
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
        }

        return "User " + user.getUsername() + " role updated to " + roleName;
    }
}
