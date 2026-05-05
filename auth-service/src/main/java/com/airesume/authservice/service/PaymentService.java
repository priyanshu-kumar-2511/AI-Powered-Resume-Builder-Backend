package com.airesume.authservice.service;

import com.airesume.authservice.dto.CreateOrderResponse;
import com.airesume.authservice.dto.SubscriptionStatusResponse;
import com.airesume.authservice.dto.VerifyPaymentRequest;
import com.airesume.authservice.dto.VerifyPaymentResponse;
import com.airesume.authservice.model.BillingCycle;
import com.airesume.authservice.model.PlanType;
import com.airesume.authservice.model.Subscription;
import com.airesume.authservice.model.SubscriptionStatus;
import com.airesume.authservice.model.User;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private static final long MONTHLY_PRICE_PAISE = 5_000L;
    private static final long YEARLY_PRICE_PAISE = 50_000L;

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

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

            return CreateOrderResponse.builder()
                    .orderId(orderId)
                    .amountInPaise(amount)
                    .currency("INR")
                    .keyId(razorpayKeyId)
                    .billingCycle(billingCycle.toUpperCase())
                    .build();
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order", e);
            String gatewayMessage = e.getMessage() != null && !e.getMessage().isBlank()
                    ? e.getMessage()
                    : "Please try again.";
            throw new RuntimeException("Payment gateway error: " + gatewayMessage, e);
        }
    }

    @Transactional
    public VerifyPaymentResponse verifyAndActivate(VerifyPaymentRequest request) {
        if (!isValidSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature())) {
            return VerifyPaymentResponse.builder()
                    .success(false)
                    .message("Payment verification failed. Invalid signature.")
                    .build();
        }

        boolean duplicatePayment = subscriptionRepository.findAll().stream()
                .anyMatch(subscription -> request.getRazorpayPaymentId().equals(subscription.getRazorpayPaymentId()));
        if (duplicatePayment) {
            return VerifyPaymentResponse.builder()
                    .success(false)
                    .message("This payment has already been processed.")
                    .build();
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        user.setSubscriptionPlan(PlanType.PREMIUM);
        userRepository.save(user);

        BillingCycle cycle = BillingCycle.valueOf(request.getBillingCycle().toUpperCase());
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = cycle == BillingCycle.YEARLY
                ? startDate.plusDays(365)
                : startDate.plusDays(30);

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(PlanType.PREMIUM)
                .billingCycle(cycle)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(startDate)
                .endDate(endDate)
                .razorpayOrderId(request.getRazorpayOrderId())
                .razorpayPaymentId(request.getRazorpayPaymentId())
                .build();
        subscriptionRepository.save(subscription);

        try {
            emailService.sendPremiumActivationEmail(user.getEmail(), user.getFullName());
        } catch (Exception e) {
            log.warn("Premium activation email failed for {}: {}", username, e.getMessage());
        }

        return VerifyPaymentResponse.builder()
                .success(true)
                .newToken(jwtService.generateTokenForUser(user))
                .message("Welcome to Premium! Your plan is now active until " + endDate.toLocalDate())
                .build();
    }

    @Transactional
    public VerifyPaymentResponse completeDevPayment(String billingCycle) {
        if (!razorpayKeyId.startsWith("rzp_test_")) {
            throw new RuntimeException("Dev payment simulation is allowed only while test Razorpay keys are active.");
        }

        String simulatedOrderId = "dev_order_" + UUID.randomUUID().toString().replace("-", "");
        String simulatedPaymentId = "dev_pay_" + UUID.randomUUID().toString().replace("-", "");

        return activatePremiumSubscription(billingCycle, simulatedOrderId, simulatedPaymentId);
    }

    public SubscriptionStatusResponse getStatus(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return subscriptionRepository
                .findTopByUserAndStatusOrderByStartDateDesc(user, SubscriptionStatus.ACTIVE)
                .or(() -> subscriptionRepository.findTopByUserAndStatusOrderByStartDateDesc(user, SubscriptionStatus.CANCELLED))
                .map(subscription -> SubscriptionStatusResponse.builder()
                        .plan(subscription.getPlan())
                        .billingCycle(subscription.getBillingCycle())
                        .status(subscription.getStatus())
                        .startDate(subscription.getStartDate())
                        .endDate(subscription.getEndDate())
                        .razorpayPaymentId(subscription.getRazorpayPaymentId())
                        .build())
                .orElse(SubscriptionStatusResponse.builder()
                        .plan(PlanType.FREE)
                        .build());
    }

    @Transactional
    public String cancelSubscription(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Subscription subscription = subscriptionRepository
                .findTopByUserAndStatusOrderByStartDateDesc(user, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No active subscription found."));

        LocalDateTime cancelledAt = LocalDateTime.now();

        user.setSubscriptionPlan(PlanType.FREE);
        userRepository.save(user);

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setEndDate(cancelledAt);
        subscriptionRepository.save(subscription);

        try {
            emailService.sendPremiumCancellationEmail(user.getEmail(), user.getFullName());
        } catch (Exception e) {
            log.warn("Premium cancellation email failed for {}: {}", username, e.getMessage());
        }

        return "Subscription cancelled successfully. Your Premium access has ended immediately.";
    }

    private boolean isValidSignature(String orderId, String paymentId, String signature) {
        try {
            String data = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).equals(signature);
        } catch (Exception e) {
            log.error("Signature verification error", e);
            return false;
        }
    }

    private VerifyPaymentResponse activatePremiumSubscription(
            String billingCycle,
            String orderId,
            String paymentId) {
        boolean duplicatePayment = subscriptionRepository.findAll().stream()
                .anyMatch(subscription -> paymentId.equals(subscription.getRazorpayPaymentId()));
        if (duplicatePayment) {
            return VerifyPaymentResponse.builder()
                    .success(false)
                    .message("This payment has already been processed.")
                    .build();
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        user.setSubscriptionPlan(PlanType.PREMIUM);
        userRepository.save(user);

        BillingCycle cycle = BillingCycle.valueOf(billingCycle.toUpperCase());
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = cycle == BillingCycle.YEARLY
                ? startDate.plusDays(365)
                : startDate.plusDays(30);

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(PlanType.PREMIUM)
                .billingCycle(cycle)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(startDate)
                .endDate(endDate)
                .razorpayOrderId(orderId)
                .razorpayPaymentId(paymentId)
                .build();
        subscriptionRepository.save(subscription);

        try {
            emailService.sendPremiumActivationEmail(user.getEmail(), user.getFullName());
        } catch (Exception e) {
            log.warn("Premium activation email failed for {}: {}", username, e.getMessage());
        }

        return VerifyPaymentResponse.builder()
                .success(true)
                .newToken(jwtService.generateTokenForUser(user))
                .message("Welcome to Premium! Your plan is now active until " + endDate.toLocalDate())
                .build();
    }
}
