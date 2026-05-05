package com.airesume.authservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service to handle email communications (OTPs, Recovery, Admin actions).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Sends a 6-digit OTP for account recovery.
     */
    public void sendOtpEmail(String to, String otp, String flowType) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("ResumeAI - Your Verification Code");
        message.setText("Hello,\n\nYour 6-digit verification code for " + flowType + " is: " + otp +
                "\n\nThis code will expire in 10 minutes.\n\nRegards,\nResumeAI Team");
        try {
            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Unable to send email. Please check your email address or try again later.");
        }
    }

    /**
     * Sends the recovered username to the user's email.
     */
    public void sendUsernameEmail(String to, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("ResumeAI - Your Recovered Username");
        message.setText("Hello,\n\nYour username associated with this account is: " + username +
                "\n\nYou can now use this to login to your dashboard.\n\nRegards,\nResumeAI Team");
        try {
            mailSender.send(message);
            log.info("Username recovery email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send Username email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Unable to send email. Please check your email address or try again later.");
        }
    }

    /**
     * Sends a welcome/thank-you email on every successful login.
     * Failure is logged but NOT thrown — login should never be blocked by a mail
     * error.
     */
    public void sendWelcomeLoginEmail(String to, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Welcome back to ResumeAI! 👋");
        message.setText(
                "Hello " + fullName + ",\n\n" +
                        "Thank you for logging into ResumeAI!\n\n" +
                        "We're glad to have you back. Here's what you can do today:\n" +
                        "  📝  Build or update your resume with AI assistance\n" +
                        "  🎨  Browse and apply professional resume templates\n" +
                        "  🎯  Match your resume against live job postings\n\n" +
                        "If this login wasn't you, please reset your password immediately:\n" +
                        "  http://localhost:4200/forgot-password\n\n" +
                        "Regards,\n" +
                        "The ResumeAI Team");
        try {
            mailSender.send(message);
            log.info("Welcome login email sent to: {}", to);
        } catch (Exception e) {
            log.warn("Could not send welcome login email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Sends a suspension notification email to a user whose account has been
     * suspended by an admin.
     * Failure is logged but NOT thrown — suspension itself should still proceed.
     *
     * @param to       The user's email address.
     * @param fullName The user's full name.
     * @param reason   The reason provided by the admin for the suspension.
     */
    public void sendSuspensionEmail(String to, String fullName, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("ResumeAI - Your Account Has Been Suspended");
        message.setText(
                "Hello " + fullName + ",\n\n" +
                        "We regret to inform you that your ResumeAI account has been suspended by our administrative team.\n\n"
                        +
                        "──────────────────────────────────────────\n" +
                        "REASON FOR SUSPENSION\n" +
                        "──────────────────────────────────────────\n" +
                        reason + "\n\n" +
                        "──────────────────────────────────────────\n" +
                        "CODE OF CONDUCT VIOLATION\n" +
                        "──────────────────────────────────────────\n" +
                        "ResumeAI maintains a strict Code of Conduct to ensure a safe and fair platform for all users. "
                        +
                        "Actions that violate our terms include, but are not limited to:\n" +
                        "  • Misuse of AI generation features for spam or harmful content\n" +
                        "  • Attempting to bypass rate limits or quota restrictions\n" +
                        "  • Sharing or distributing other users' private resume data\n" +
                        "  • Using the platform for fraudulent or deceptive job applications\n" +
                        "  • Any activity that violates applicable laws or regulations\n\n" +
                        "If you believe this suspension was made in error, or if you would like to appeal this decision, "
                        +
                        "please contact our support team at kumarpriyanshu77828@gmail.com and include your username and the reason "
                        +
                        "for your appeal.\n\n" +
                        "Your account data is preserved and will remain accessible to our team during the review period.\n\n"
                        +
                        "Regards,\n" +
                        "The ResumeAI Trust & Safety Team");
        try {
            mailSender.send(message);
            log.info("Suspension email sent to: {}", to);
        } catch (Exception e) {
            log.warn("Could not send suspension email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Sends a reactivation notification email when an admin restores a user's
     * account.
     * Failure is logged but NOT thrown.
     *
     * @param to       The user's email address.
     * @param fullName The user's full name.
     */
    public void sendReactivationEmail(String to, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("ResumeAI - Your Account Has Been Reactivated");
        message.setText(
                "Hello " + fullName + ",\n\n" +
                        "Great news! Your ResumeAI account has been reactivated by our administrative team.\n\n" +
                        "You may now log in and resume using all platform features:\n" +
                        "  http://localhost:4200/login\n\n" +
                        "Please ensure your activity complies with our Code of Conduct going forward. " +
                        "Repeated violations may result in permanent account removal.\n\n" +
                        "If you have any questions, please contact kumarpriyanshu77828@gmail.com.\n\n" +
                        "Regards,\n" +
                        "The ResumeAI Trust & Safety Team");
        try {
            mailSender.send(message);
            log.info("Reactivation email sent to: {}", to);
        } catch (Exception e) {
            log.warn("Could not send reactivation email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Sends a notification when a user is promoted to Admin.
     * This grants them access to the Administrative Dashboard and specialized tools.
     * 
     * @param to       The recipient's email address.
     * @param fullName The recipient's full name.
     */
    public void sendAdminPromotionEmail(String to, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("ResumeAI - You have been promoted to Admin! 🎖️");
        message.setText(
                "Hello " + fullName + ",\n\n" +
                        "Congratulations! You have been granted Administrative privileges on ResumeAI.\n\n" +
                        "You can now access the Admin Dashboard to manage users, templates, and platform analytics:\n" +
                        "  http://localhost:4200/admin\n\n" +
                        "Please use your new privileges responsibly and ensure you follow the Admin guidelines.\n\n" +
                        "Regards,\n" +
                        "The ResumeAI Management Team");
        try {
            mailSender.send(message);
            log.info("Admin promotion email sent to: {}", to);
        } catch (Exception e) {
            log.warn("Could not send admin promotion email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Sends a notification when a user's Admin privileges are revoked.
     * The user will retain regular access to the platform.
     * 
     * @param to       The recipient's email address.
     * @param fullName The recipient's full name.
     */
    public void sendAdminDemotionEmail(String to, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("ResumeAI - Your Admin Access Has Been Removed");
        message.setText(
                "Hello " + fullName + ",\n\n" +
                        "This is to let you know that your ResumeAI account no longer has Administrative privileges.\n\n" +
                        "You can still sign in and use the platform as a regular user:\n" +
                        "  http://localhost:4200/login\n\n" +
                        "If you believe this change was made in error, please contact the platform administrator.\n\n" +
                        "Regards,\n" +
                        "The ResumeAI Management Team");
        try {
            mailSender.send(message);
            log.info("Admin demotion email sent to: {}", to);
        } catch (Exception e) {
            log.warn("Could not send admin demotion email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Sends a notification when a user is upgraded to Premium.
     * Highlights the core benefits of the Premium plan.
     * 
     * @param to       The user's email address.
     * @param fullName The user's full name.
     */
    public void sendPremiumActivationEmail(String to, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("ResumeAI - Your Premium Plan is Active! ✨");
        message.setText(
                "Hello " + fullName + ",\n\n" +
                        "Great news! Your ResumeAI account has been upgraded to PREMIUM.\n\n" +
                        "You now have unlimited access to all professional features:\n" +
                        "  🚀  Unlimited AI Resume Generations\n" +
                        "  📊  Advanced ATS Matching & Suggestions\n" +
                        "  🎨  Access to all Premium Templates\n" +
                        "  📥  Unlimited Document Exports\n\n" +
                        "Login now to start building your dream resume:\n" +
                        "  http://localhost:4200/login\n\n" +
                        "Regards,\n" +
                        "The ResumeAI Team");
        try {
            mailSender.send(message);
            log.info("Premium activation email sent to: {}", to);
        } catch (Exception e) {
            log.warn("Could not send premium activation email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Sends a notification when a user's Premium subscription is cancelled or expired.
     * Invites the user to re-subscribe from the pricing page.
     * 
     * @param to       The user's email address.
     * @param fullName The user's full name.
     */
    public void sendPremiumCancellationEmail(String to, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("ResumeAI - Your Premium Plan Has Been Cancelled");
        message.setText(
                "Hello " + fullName + ",\n\n" +
                        "Your ResumeAI Premium subscription has been cancelled successfully.\n\n" +
                        "Your account is now on the Free plan. You can upgrade again anytime from the pricing page:\n" +
                        "  http://localhost:4200/pricing\n\n" +
                        "If you did not make this change, please contact support immediately.\n\n" +
                        "Regards,\n" +
                        "The ResumeAI Team");
        try {
            mailSender.send(message);
            log.info("Premium cancellation email sent to: {}", to);
        } catch (Exception e) {
            log.warn("Could not send premium cancellation email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Sends a security alert email when an Admin logs in.
     */
    public void sendAdminLoginAlertEmail(String to, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("SECURITY ALERT: Admin Login Detected 🚨");
        message.setText(
                "Hello " + fullName + ",\n\n" +
                        "This is a security notification to inform you that your Admin account has just been accessed.\n\n" +
                        "Login Details:\n" +
                        "  • Account: " + fullName + " (ADMIN)\n" +
                        "  • Platform: ResumeAI Management Console\n" +
                        "  • Timestamp: " + new java.util.Date() + "\n\n" +
                        "If this was not you, your account may be compromised. Please contact technical support and " +
                        "reset your password immediately.\n\n" +
                        "Regards,\n" +
                        "ResumeAI Security System");
        try {
            mailSender.send(message);
            log.info("Admin login alert email sent to: {}", to);
        } catch (Exception e) {
            log.warn("Could not send admin login alert email to {}: {}", to, e.getMessage());
        }
    }
}
