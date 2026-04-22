package com.airesume.authservice.service;

import com.airesume.authservice.dto.*;
import com.airesume.authservice.model.Role;
import com.airesume.authservice.model.User;
import com.airesume.authservice.model.VerificationOtp;
import com.airesume.authservice.repository.RoleRepository;
import com.airesume.authservice.repository.UserRepository;
import com.airesume.authservice.repository.UserQuotaRepository;
import com.airesume.authservice.model.UserQuota;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Main Service for Authentication and Identity Management logic.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final UserQuotaRepository userQuotaRepository;

    /**
     * Authenticates a user and returns a JWT token.
     */
    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Username not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Incorrect Password");
        }

        return jwtService.generateToken(user.getUsername());
    }

    /**
     * Registers a new user with strict validation and default roles.
     */
    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return "Username already exists";
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return "Email already registered";
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_USER")));

        User user = User.builder()
                .fullName(request.getFullName())
                .age(request.getAge())
                .mobileNumber(request.getMobileNumber())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .build();
        
        user.getRoles().add(userRole);
        User savedUser = userRepository.save(user);

        // Initialize User Quota
        UserQuota quota = UserQuota.builder()
                .user(savedUser)
                .build();
        userQuotaRepository.save(quota);

        return "User registered successfully";
    }

    /**
     * Initiates Username Recovery by validating email/password and sending OTP.
     */
    @Transactional
    public String initiateUsernameRecovery(UsernameRecoveryRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email not registered"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Incorrect Password");
        }

        String otp = otpService.generateAndSaveOtp(user, VerificationOtp.OtpType.USERNAME_RECOVERY);
        emailService.sendOtpEmail(user.getEmail(), otp, "Username Recovery");

        return "OTP sent to your registered email";
    }

    /**
     * Verifies OTP and sends the recovered username via email.
     */
    @Transactional
    public String verifyUsernameRecovery(OtpVerificationRequest request) {
        User user = userRepository.findByEmail(request.getIdentifier())
                .orElseThrow(() -> new RuntimeException("Email not registered"));

        if (!otpService.validateOtp(user, request.getOtp(), VerificationOtp.OtpType.USERNAME_RECOVERY)) {
            throw new RuntimeException("Invalid or Expired OTP");
        }

        emailService.sendUsernameEmail(user.getEmail(), user.getUsername());
        return "Username has been sent to your registered email";
    }

    /**
     * Initiates Password Reset by validating username/email and sending OTP.
     */
    @Transactional
    public String initiatePasswordReset(PasswordResetInitiateRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Username not found"));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail())) {
            throw new RuntimeException("Username and Email do not match");
        }

        String otp = otpService.generateAndSaveOtp(user, VerificationOtp.OtpType.PASSWORD_RESET);
        emailService.sendOtpEmail(user.getEmail(), otp, "Password Reset");

        return "OTP sent to your registered email";
    }

    /**
     * Verifies OTP and resets the password.
     */
    @Transactional
    public String resetPassword(OtpVerificationRequest request) {
        User user = userRepository.findByUsername(request.getIdentifier())
                .orElseThrow(() -> new RuntimeException("Username not found"));

        if (!otpService.validateOtp(user, request.getOtp(), VerificationOtp.OtpType.PASSWORD_RESET)) {
            throw new RuntimeException("Invalid or Expired OTP");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return "Password updated successfully";
    }

    /**
     * Updates the user's profile information.
     */
    @Transactional
    public String updateProfile(String username, ProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Username not found"));
        
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getAge() != null) user.setAge(request.getAge());
        if (request.getMobileNumber() != null) user.setMobileNumber(request.getMobileNumber());
        
        userRepository.save(user);
        return "Profile updated successfully";
    }

    /**
     * Changes the user's password using authentication.
     */
    @Transactional
    public String changePassword(String username, PasswordChangeRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Username not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Incorrect current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return "Password changed successfully";
    }

    /**
     * Updates the user's subscription plan.
     */
    @Transactional
    public String updateSubscription(String username, com.airesume.authservice.model.PlanType newPlan) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Username not found"));
                
        user.setSubscriptionPlan(newPlan);
        userRepository.save(user);
        return "Subscription updated to " + newPlan.name();
    }

    /**
     * Soft deletes (deactivates) a user account.
     */
    @Transactional
    public String deactivateAccount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Username not found"));
        
        user.setActive(false);
        userRepository.save(user);
        return "Account successfully deactivated";
    }

    /**
     * Fetches the full profile of the authenticated user.
     */
    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserProfileResponse.builder()
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .age(user.getAge())
                .subscriptionPlan(user.getSubscriptionPlan())
                .isActive(user.isActive())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }

    /**
     * Issues a fresh JWT token for the user.
     */
    public String refreshToken(String username) {
        return jwtService.generateToken(username);
    }

    // --- ADMIN OPERATIONS ---

    /**
     * Admin only: Get total user list.
     */
    public List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserProfileResponse.builder()
                        .username(user.getUsername())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .subscriptionPlan(user.getSubscriptionPlan())
                        .isActive(user.isActive())
                        .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Admin only: Suspend or reactivate user accounts.
     */
    @Transactional
    public String updateUserStatus(String username, boolean active) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(active);
        userRepository.save(user);
        return "User status updated to: " + (active ? "ACTIVE" : "SUSPENDED");
    }

    /**
     * Admin only: Promote or demote user roles.
     */
    @Transactional
    public String updateUserRole(String username, String roleName) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role " + roleName + " not found"));
        
        user.getRoles().clear();
        user.getRoles().add(role);
        userRepository.save(user);
        return "User role updated to: " + roleName;
    }

    /**
     * Admin only: Filter users by their role (e.g., ROLE_USER, ROLE_ADMIN).
     */
    public List<UserProfileResponse> getUsersByRole(String roleName) {
        return userRepository.findAllByRoles_Name(roleName).stream()
                .map(user -> mapToProfileResponse(user))
                .collect(Collectors.toList());
    }

    /**
     * Admin only: Filter users by their subscription tier.
     */
    public List<UserProfileResponse> getUsersByPlan(com.airesume.authservice.model.PlanType plan) {
        return userRepository.findBySubscriptionPlan(plan).stream()
                .map(user -> mapToProfileResponse(user))
                .collect(Collectors.toList());
    }

    /**
     * Admin only: Permanently remove a user from the database.
     */
    @Transactional
    public String deleteUserPermanently(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User ID " + userId + " not found");
        }
        userRepository.deleteById(userId);
        return "User with ID " + userId + " permanently deleted";
    }

    /**
     * Validates a token and returns the corresponding username.
     * Used by other microservices via the /validate endpoint.
     */
    public String validateToken(String token) {
        if (jwtService.validateToken(token)) {
            return jwtService.extractUsername(token);
        }
        throw new RuntimeException("Invalid or Expired Token");
    }

    /**
     * Helper to map User entity to UserProfileResponse DTO.
     */
    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .subscriptionPlan(user.getSubscriptionPlan())
                .isActive(user.isActive())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }
}
