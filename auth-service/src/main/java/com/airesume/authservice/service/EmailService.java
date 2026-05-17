package com.airesume.authservice.service;

import com.airesume.authservice.config.RabbitMQConfig;
import com.airesume.authservice.dto.EmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service to handle email communications (OTPs, Recovery, Admin actions).
 * Instead of direct SMTP network block, it publishes email requests asynchronously to RabbitMQ.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final RabbitTemplate rabbitTemplate;
    private static final String DEAR = "Dear ";

    @Value("${app.frontend.public-url:http://localhost:4200}")
    private String frontendPublicUrl;

    /**
     * Publishes email message payload to RabbitMQ.
     */
    private void publishEmail(String to, String subject, String body) {
        log.info("[RABBITMQ] Preparing email task publication for: {}", to);
        try {
            EmailMessage message = EmailMessage.builder()
                    .to(to)
                    .subject(subject)
                    .body(wrapInTemplate(subject, body))
                    .isHtml(true)
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.EMAIL_ROUTING_KEY,
                    message
            );
            log.info("[RABBITMQ] Successfully published email task to queue for: {}", to);
        } catch (Exception e) {
            log.error("[RABBITMQ] Failed to publish email task for: {}. Error: {}", to, e.getMessage(), e);
        }
    }

    /**
     * Wraps the raw message body in a premium HTML/CSS template.
     */
    private String wrapInTemplate(String title, String body) {
        String formattedBody = body.replace("\n", "<br>");
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "<style>" +
               "  body { font-family: 'Inter', 'Segoe UI', Arial, sans-serif; background-color: #0f172a; margin: 0; padding: 0; color: #f8fafc; }" +
               "  .container { max-width: 600px; margin: 40px auto; background-color: #1e293b; border-radius: 12px; overflow: hidden; border: 1px solid #334155; box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.5); }" +
               "  .header { background: linear-gradient(135deg, #0d9488, #0891b2); padding: 35px 20px; text-align: center; }" +
               "  .header h1 { margin: 0; color: #ffffff; font-size: 26px; font-weight: 800; letter-spacing: 2px; text-transform: uppercase; }" +
               "  .content { padding: 45px; line-height: 1.7; font-size: 16px; color: #cbd5e1; }" +
               "  .content h2 { color: #5eead4; margin-top: 0; font-size: 22px; font-weight: 700; border-bottom: 1px solid #334155; padding-bottom: 15px; margin-bottom: 25px; }" +
               "  .footer { background-color: #0f172a; padding: 25px; text-align: center; font-size: 13px; color: #64748b; border-top: 1px solid #334155; }" +
               "  .button { display: inline-block; padding: 14px 28px; background-color: #0d9488; color: #ffffff; text-decoration: none; border-radius: 8px; font-weight: 600; margin-top: 25px; text-align: center; min-width: 180px; }" +
               "  .highlight { color: #5eead4; font-weight: 700; }" +
               "  .divider { height: 1px; background-color: #334155; margin: 30px 0; }" +
               "</style>" +
               "</head>" +
               "<body>" +
               "  <div class='container'>" +
               "    <div class='header'><h1>ResumeAI</h1></div>" +
               "    <div class='content'>" +
               "      <h2>" + title + "</h2>" +
               "      " + formattedBody + "" +
               "    </div>" +
               "    <div class='footer'>" +
               "      &copy; " + java.time.Year.now().getValue() + " ResumeAI Technology Solutions. All rights reserved.<br>" +
               "      Technical Inquiry? Reach out to <a href='mailto:kumarpriyanshu77828@gmail.com' style='color: #0d9488; text-decoration: none;'>support@airesume.com</a>" +
               "    </div>" +
               "  </div>" +
               "</body>" +
               "</html>";
    }

    /**
     * Sends a 6-digit OTP for account recovery.
     */
    public void sendOtpEmail(String to, String otp, String flowType) {
        String subject = "Security Verification: One-Time Password (OTP)";
        String body = "Dear Valued Member,<br><br>As part of our commitment to account security, please use the following one-time password to complete your <span class='highlight'>" + flowType + "</span> request:<br>" +
                "<div style='background: rgba(94, 234, 212, 0.05); border: 1px dashed #5eead4; border-radius: 8px; font-size: 36px; font-weight: 800; color: #5eead4; padding: 25px; margin: 30px 0; letter-spacing: 8px; text-align: center;'>" + otp + "</div>" +
                "For your protection, this code will remain active for <span class='highlight'>10 minutes</span>. If you did not initiate this request, please disregard this email or contact security immediately.<br><br>Best regards,<br>The ResumeAI Security Team";
        publishEmail(to, subject, body);
    }

    /**
     * Sends the recovered username to the user's email.
     */
    public void sendUsernameEmail(String to, String username) {
        String subject = "Account Information: Username Recovery";
        String body = "Dear User,<br><br>Per your recent request, we have successfully retrieved the username associated with your account:<br><br>" +
                "<div style='text-align: center; font-size: 20px; font-weight: 600; color: #5eead4; margin: 20px 0;'>[" + username + "]</div>" +
                "You may now use these credentials to access your professional dashboard. We recommend keeping this information confidential.<br><br>Sincerely,<br>The ResumeAI Support Team";
        publishEmail(to, subject, body);
    }

    /**
     * Sends a welcome/thank-you email on every successful login.
     */
    public void sendWelcomeLoginEmail(String to, String fullName) {
        String subject = "Secure Login Notification: Welcome Back";
        String body = DEAR + "<span class='highlight'>" + fullName + "</span>,<br><br>" +
                "We are writing to confirm a successful login to your ResumeAI account. It is our pleasure to have you back on the platform.<br><br>" +
                "Your workspace is ready for you to continue your professional journey:<br>" +
                "  • 📝  <span class='highlight'>Resume Engineering:</span> Refine your document with advanced AI modules.<br>" +
                "  • 🎨  <span class='highlight'>Visual Templates:</span> Access our curated library of premium designs.<br>" +
                "  • 🎯  <span class='highlight'>ATS Optimization:</span> Match your profile against top-tier opportunities.<br><br>" +
                "If this activity was unauthorized, please secure your account immediately by resetting your password:<br>" +
                "<div style='text-align: center;'><a href='" + frontendUrl("/forgot-password") + "' class='button'>Secure My Account</a></div><br><br>" +
                "Best regards,<br>The ResumeAI Administration";
        publishEmail(to, subject, body);
    }

    /**
     * Sends a suspension notification email.
     */
    public void sendSuspensionEmail(String to, String fullName, String reason) {
        String subject = "Official Notice: Account Suspension";
        String body = DEAR + fullName + ",<br><br>" +
                "This is an official communication to inform you that your ResumeAI account has been suspended by our administrative department, effective immediately.<br><br>" +
                "<div style='background: rgba(239, 68, 68, 0.1); border-left: 4px solid #ef4444; padding: 20px; margin: 25px 0; color: #f87171;'>" +
                "<strong>RATIONALE FOR SUSPENSION:</strong><br>" + reason + "</div>" +
                "ResumeAI maintains a standard of professional integrity. If you wish to contest this decision or require further clarification, please submit a formal appeal to our safety team.<br><br>" +
                "Sincerely,<br>ResumeAI Trust & Safety Division";
        publishEmail(to, subject, body);
    }

    /**
     * Sends a reactivation notification email.
     */
    public void sendReactivationEmail(String to, String fullName) {
        String subject = "Status Update: Account Reactivation";
        String body = DEAR + fullName + ",<br><br>" +
                "Following a review of your account status, we are pleased to inform you that your ResumeAI access has been fully restored.<br><br>" +
                "You may resume all platform activities by logging into your dashboard:<br>" +
                "<div style='text-align: center;'><a href='" + frontendUrl("/login") + "' class='button'>Return to Dashboard</a></div><br><br>" +
                "We appreciate your patience and look forward to your continued use of our services.<br><br>Best regards,<br>ResumeAI Administration";
        publishEmail(to, subject, body);
    }

    /**
     * Sends a notification when a user is promoted to Admin.
     */
    public void sendAdminPromotionEmail(String to, String fullName) {
        String subject = "Notification of Administrative Appointment";
        String body = DEAR + fullName + ",<br><br>" +
                "We are pleased to announce that you have been provisioned with Administrative privileges on the ResumeAI platform.<br><br>" +
                "Your new role grants you access to specialized tools for user management, template curation, and platform-wide analytics. You may access your management console here:<br>" +
                "<div style='text-align: center;'><a href='" + frontendUrl("/admin") + "' class='button'>Enter Management Console</a></div><br><br>" +
                "Please ensure all administrative actions align with our internal governance policies.<br><br>Regards,<br>ResumeAI Operations Team";
        publishEmail(to, subject, body);
    }

    /**
     * Sends a notification when a user's Admin privileges are revoked.
     */
    public void sendAdminDemotionEmail(String to, String fullName) {
        String subject = "Notification of Role Adjustment";
        String body = DEAR + fullName + ",<br><br>" +
                "This communication serves to notify you that your Administrative access has been withdrawn. Your account has been successfully transitioned back to standard user status.<br><br>" +
                "You will retain full access to all resume-building and career-development features. If you require assistance regarding this adjustment, please contact your regional administrator.<br><br>" +
                "Sincerely,<br>ResumeAI Human Resources";
        publishEmail(to, subject, body);
    }

    /**
     * Sends a notification when a user is upgraded to Premium.
     */
    public void sendPremiumActivationEmail(String to, String fullName) {
        String subject = "Confirmation of Premium Tier Activation";
        String body = DEAR + "<span class='highlight'>" + fullName + "</span>,<br><br>" +
                "We are delighted to confirm that your account has been successfully upgraded to <span class='highlight'>PREMIUM</span> status.<br><br>" +
                "As a Premium member, you now hold the most advanced tools to accelerate your professional growth:<br>" +
                "  🚀  <span class='highlight'>Unrestricted AI Iterations:</span> Generate resumes without volume limitations.<br>" +
                "  📊  <span class='highlight'>Deep-Level ATS Analysis:</span> Access granular matching data for targeted applications.<br>" +
                "  🎨  <span class='highlight'>Elite Template Library:</span> Deploy your profile using our most exclusive designs.<br><br>" +
                "<div style='text-align: center;'><a href='" + frontendUrl("/login") + "' class='button'>Launch Premium Workspace</a></div><br><br>" +
                "Thank you for choosing ResumeAI as your career partner.<br><br>Best regards,<br>The ResumeAI Success Team";
        publishEmail(to, subject, body);
    }

    /**
     * Sends a notification when a user's Premium subscription is cancelled.
     */
    public void sendPremiumCancellationEmail(String to, String fullName) {
        String subject = "Acknowledgment: Premium Plan Cancellation";
        String body = DEAR + fullName + ",<br><br>" +
                "This email confirms that your ResumeAI Premium subscription has been successfully cancelled as per your request.<br><br>" +
                "Your account has been transitioned to the standard Free tier. You may re-subscribe at any time to regain access to advanced analytics and unlimited exports:<br>" +
                "<div style='text-align: center;'><a href='" + frontendUrl("/pricing") + "' class='button'>Review Membership Plans</a></div><br><br>" +
                "Regards,<br>The ResumeAI Support Team";
        publishEmail(to, subject, body);
    }

    /**
     * Sends a security alert email when an Admin logs in.
     */
    public void sendAdminLoginAlertEmail(String to, String fullName) {
        String subject = "Urgent: Administrative Login Security Alert";
        String body = DEAR + fullName + ",<br><br>" +
                "This is an automated security alert to inform you that your Administrative account was recently accessed.<br><br>" +
                "<div style='background: rgba(234, 179, 8, 0.1); border: 1px solid rgba(234, 179, 8, 0.3); padding: 20px; margin: 25px 0;'>" +
                "<strong>AUTHENTICATION DETAILS:</strong><br>" +
                "• Identity: " + fullName + " (ADMIN)<br>" +
                "• Timestamp: " + new java.util.Date() + "</div>" +
                "If you did not authorize this login, please initiate an immediate security protocol by resetting your credentials and contacting the IT Safety Department.<br><br>" +
                "Sincerely,<br>ResumeAI Security Infrastructure";
        publishEmail(to, subject, body);
    }

    private String frontendUrl(String path) {
        return stripTrailingSlashes(frontendPublicUrl) + path;
    }

    private String stripTrailingSlashes(String value) {
        int end = value == null ? 0 : value.length();
        while (end > 0 && value.charAt(end - 1) == '/') {
            end--;
        }
        return end == 0 ? "http://localhost:4200" : value.substring(0, end);
    }
}
