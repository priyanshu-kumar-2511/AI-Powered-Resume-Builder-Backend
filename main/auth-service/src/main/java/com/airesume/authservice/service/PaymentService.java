package com.airesume.authservice.service;

import com.airesume.authservice.dto.*;
import com.airesume.authservice.model.*;
import com.airesume.authservice.repository.SubscriptionRepository;
import com.airesume.authservice.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * Service handling Razorpay payment creation, verification, and subscription lifecycle.
 *
 * Pricing:
 *   Monthly : ₹50  → 5000 paise
 *   Yearly  : ₹500 → 50000 paise  (₹50×12 = ₹600 → 17% off)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    // ── Pricing constants ─────────────────────────────────────────────────────
    private static final long MONTHLY_PRICE_PAISE = 5_000L;   // ₹50
    private static final long YEARLY_PRICE_PAISE  = 50_000L;  // ₹500

    // ── Dependencies ─────────────────────────────────────────────────────────
    private final UserRepository         userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final JwtService             jwtService;
    private final EmailService           emailService;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    // ── Create Order ──────────────────────────────────────────────────────────

    /**
     * Creates a Razorpay order and returns orderId + amount for the frontend checkout.
     */
    public CreateOrderResponse createOrder(String billingCycle) {
        long amount = "YEARLY".equalsIgnoreCase(billingCycle)
                ? YEARLY_PRICE_PAISE
                : MONTHLY_PRICE_PAISE;

        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject options = new JSONObject();
            options.put("amount", amount);
            options.put("currency", "INR");
            options.put("receipt", "rcpt_" + System.currentTimeMillis());
            options.put("payment_capture", 1);

            Order order = client.orders.create(options);
            String orderId = order.get("id");

            log.info("Razorpay order created: {} for cycle: {} amount: {} paise", orderId, billingCycle, amount);

            return CreateOrderResponse.builder()
                    .orderId(orderId)
                    .amountInPaise(amount)
                    .currency("INR")
                    .keyId(razorpayKeyId)
                    .billingCycle(billingCycle.toUpperCase())
                    .build();

        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order", e);
            throw new RuntimeException("Payment gateway error. Please try again.", e);
        }
    }

    // ── Verify & Activate ────────────────────────────────────────────────────

    /**
     * Verifies the Razorpay HMAC-SHA256 signature, upgrades the user to PREMIUM,
     * saves the subscription record, and returns a fresh JWT.
     */
    @Transactional
    public VerifyPaymentResponse verifyAndActivate(VerifyPaymentRequest req) {
        // 1. Verify signature
        if (!isValidSignature(req.getRazorpayOrderId(), req.getRazorpayPaymentId(), req.getRazorpaySignature())) {
            log.warn("Payment signature verification failed for orderId: {}", req.getRazorpayOrderId());
            return VerifyPaymentResponse.builder()
                    .success(false)
                    .message("Payment verification failed. Invalid signature.")
                    .build();
        }

        // 2. Idempotency check — prevent double-processing
        if (subscriptionRepository.findAll().stream()
                .anyMatch(s -> req.getRazorpayPaymentId().equals(s.getRazorpayPaymentId()))) {
            log.warn("Duplicate payment verification attempt for paymentId: {}", req.getRazorpayPaymentId());
            return VerifyPaymentResponse.builder()
                    .success(false)
                    .message("This payment has already been processed.")
                    .build();
        }

        // 3. Get current user from security context
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // 4. Upgrade plan in users table
        user.setSubscriptionPlan(PlanType.PREMIUM);
        userRepository.save(user);

        // 5. Calculate subscription dates
        BillingCycle cycle = BillingCycle.valueOf(req.getBillingCycle().toUpperCase());
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate   = cycle == BillingCycle.YEARLY
                ? startDate.plusDays(365)
                : startDate.plusDays(30);

        // 6. Save subscription record
        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(PlanType.PREMIUM)
                .billingCycle(cycle)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(startDate)
                .endDate(endDate)
                .razorpayOrderId(req.getRazorpayOrderId())
                .razorpayPaymentId(req.getRazorpayPaymentId())
                .build();
        subscriptionRepository.save(subscription);

        // 7. Issue new JWT with PREMIUM claim
        String newToken = jwtService.generateTokenForUser(user);

        // 8. Send welcome email (async — don't block)
        try {
            emailService.sendPremiumActivationEmail(user.getEmail(), user.getFullName());
        } catch (Exception e) {
            log.warn("Welcome email failed for user {}: {}", username, e.getMessage());
        }

        log.info("User {} upgraded to PREMIUM. Cycle: {}. Expires: {}", username, cycle, endDate);

        return VerifyPaymentResponse.builder()
                .success(true)
                .newToken(newToken)
                .message("Welcome to Premium! Your plan is now active until " + endDate.toLocalDate())
                .build();
    }

    // ── Get Status ───────────────────────────────────────────────────────────

    public SubscriptionStatusResponse getStatus(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return subscriptionRepository
                .findTopByUserAndStatusOrderByStartDateDesc(user, SubscriptionStatus.ACTIVE)
                .or(() -> subscriptionRepository.findTopByUserAndStatusOrderByStartDateDesc(user, SubscriptionStatus.CANCELLED))
                .map(s -> SubscriptionStatusResponse.builder()
                        .plan(s.getPlan())
                        .billingCycle(s.getBillingCycle())
                        .status(s.getStatus())
                        .startDate(s.getStartDate())
                        .endDate(s.getEndDate())
                        .razorpayPaymentId(s.getRazorpayPaymentId())
                        .build())
                .orElse(SubscriptionStatusResponse.builder()
                        .plan(PlanType.FREE)
                        .build());
    }

    // ── Cancel ───────────────────────────────────────────────────────────────

    @Transactional
    public String cancelSubscription(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Subscription sub = subscriptionRepository
                .findTopByUserAndStatusOrderByStartDateDesc(user, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No active subscription found."));

        sub.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(sub);

        log.info("User {} cancelled subscription. Remains PREMIUM until {}", username, sub.getEndDate());

        return "Subscription cancelled. You will remain on Premium until " + sub.getEndDate().toLocalDate() + ".";
    }

    // ── Admin Methods ────────────────────────────────────────────────────────

    public org.springframework.data.domain.Page<AdminSubscriptionResponse> getAdminSubscriptions(org.springframework.data.domain.Pageable pageable) {
        return subscriptionRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(s -> AdminSubscriptionResponse.builder()
                        .id(s.getId())
                        .userId(s.getUser().getId())
                        .username(s.getUser().getUsername())
                        .fullName(s.getUser().getFullName())
                        .plan(s.getPlan())
                        .billingCycle(s.getBillingCycle())
                        .status(s.getStatus())
                        .startDate(s.getStartDate())
                        .endDate(s.getEndDate())
                        .razorpayOrderId(s.getRazorpayOrderId())
                        .razorpayPaymentId(s.getRazorpayPaymentId())
                        .createdAt(s.getCreatedAt())
                        .build());
    }

    public SubscriptionStatsResponse getStats() {
        long active    = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
        long cancelled = subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED);
        long expired   = subscriptionRepository.countByStatus(SubscriptionStatus.EXPIRED);

        // Calculate revenue
        long monthlyCount = subscriptionRepository.findAll().stream()
                .filter(s -> s.getBillingCycle() == BillingCycle.MONTHLY).count();
        long yearlyCount = subscriptionRepository.findAll().stream()
                .filter(s -> s.getBillingCycle() == BillingCycle.YEARLY).count();
        
        long totalRevenue = (monthlyCount * MONTHLY_PRICE_PAISE) + (yearlyCount * YEARLY_PRICE_PAISE);

        return SubscriptionStatsResponse.builder()
                .totalActiveSubscriptions(active)
                .totalCancelledSubscriptions(cancelled)
                .totalExpiredSubscriptions(expired)
                .totalRevenueInPaise(totalRevenue)
                .planDistribution(java.util.Map.of(
                        "MONTHLY", monthlyCount,
                        "YEARLY", yearlyCount
                ))
                .build();
    }

    // ── Signature Verification ───────────────────────────────────────────────

    private boolean isValidSignature(String orderId, String paymentId, String signature) {
        try {
            String data = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String expected = HexFormat.of().formatHex(hash);
            return expected.equals(signature);
        } catch (Exception e) {
            log.error("Signature verification error", e);
            return false;
        }
    }
}
