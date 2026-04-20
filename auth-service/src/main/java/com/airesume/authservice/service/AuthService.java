package com.airesume.authservice.service;

import com.airesume.authservice.dto.*;
import com.airesume.authservice.model.Role;
import com.airesume.authservice.model.User;
import com.airesume.authservice.model.VerificationOtp;
import com.airesume.authservice.repository.RoleRepository;
import com.airesume.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .enabled(true)
                .build();
        
        user.getRoles().add(userRole);
        userRepository.save(user);

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
}
