package com.airesume.paymentservice.service;

import com.airesume.paymentservice.dto.CreateOrderResponse;
import com.airesume.paymentservice.dto.SubscriptionStatusResponse;
import com.airesume.paymentservice.dto.AdminSubscriptionResponse;
import com.airesume.paymentservice.dto.SubscriptionStats;
import com.airesume.paymentservice.dto.VerifyPaymentRequest;
import com.airesume.paymentservice.dto.VerifyPaymentResponse;
import com.airesume.paymentservice.model.BillingCycle;
import com.airesume.paymentservice.model.PlanType;
import com.airesume.paymentservice.model.Subscription;
import com.airesume.paymentservice.model.SubscriptionStatus;
import com.airesume.paymentservice.repository.SubscriptionRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

import com.airesume.paymentservice.client.AuthServiceClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private static final long MONTHLY_PRICE_PAISE = 5_000L;
    private static final long YEARLY_PRICE_PAISE = 50_000L;

    // Repository for subscription history and current status
    private final SubscriptionRepository subscriptionRepository;
    
    // Feign client to update user plan in Auth Service
    private final AuthServiceClient authServiceClient;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    /**
     * Step 1: Create a Razorpay Order.
     * This is called by the frontend before opening the Razorpay checkout popup.
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

            return CreateOrderResponse.builder()
                    .orderId(orderId)
                    .amountInPaise(amount)
                    .currency("INR")
                    .keyId(razorpayKeyId)
                    .billingCycle(billingCycle.toUpperCase())
                    .build();
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order", e);
            throw new RuntimeException("Payment gateway error: " + e.getMessage(), e);
        }
    }

    /**
     * Step 2: Verify the payment and activate the subscription.
     * Called after the user completes the payment in the Razorpay popup.
     * Uses HMAC-SHA256 signature verification for security.
     */
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

        if (subscriptionRepository.existsByRazorpayPaymentId(request.getRazorpayPaymentId())) {
            return VerifyPaymentResponse.builder()
                    .success(false)
                    .message("This payment has already been processed.")
                    .build();
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        BillingCycle cycle = BillingCycle.valueOf(request.getBillingCycle().toUpperCase());
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = cycle == BillingCycle.YEARLY
                ? startDate.plusDays(365)
                : startDate.plusDays(30);

        Subscription subscription = Subscription.builder()
                .username(username)
                .plan(PlanType.PREMIUM)
                .billingCycle(cycle)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(startDate)
                .endDate(endDate)
                .razorpayOrderId(request.getRazorpayOrderId())
                .razorpayPaymentId(request.getRazorpayPaymentId())
                .build();
        subscriptionRepository.save(subscription);

        // Call Auth Service to update user plan
        var updatePlanResponse = authServiceClient.updatePlan(
                new AuthServiceClient.UpdatePlanRequest(username, PlanType.PREMIUM)
        );
        
        // TODO: Call Notification Service to send email

        return VerifyPaymentResponse.builder()
                .success(true)
                .message("Welcome to Premium! Your plan is now active until " + endDate.toLocalDate())
                .newToken(updatePlanResponse.get("token"))
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
        return subscriptionRepository
                .findTopByUsernameAndStatusOrderByStartDateDesc(username, SubscriptionStatus.ACTIVE)
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
        Subscription subscription = subscriptionRepository
                .findTopByUsernameAndStatusOrderByStartDateDesc(username, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No active subscription found."));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setEndDate(LocalDateTime.now());
        subscriptionRepository.save(subscription);

        // Call Auth Service to demote user to FREE
        authServiceClient.updatePlan(new AuthServiceClient.UpdatePlanRequest(username, PlanType.FREE));

        return "Subscription cancelled successfully.";
    }

    public Page<AdminSubscriptionResponse> getSubscriptions(Pageable pageable) {
        return subscriptionRepository.findAll(pageable)
                .map(s -> AdminSubscriptionResponse.builder()
                        .id(s.getId())
                        .username(s.getUsername())
                        .fullName(s.getUsername()) // Fallback to username
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

    public SubscriptionStats getStats() {
        java.util.List<Subscription> all = subscriptionRepository.findAll();
        
        long active = all.stream().filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE).count();
        long cancelled = all.stream().filter(s -> s.getStatus() == SubscriptionStatus.CANCELLED).count();
        long expired = all.stream().filter(s -> s.getStatus() == SubscriptionStatus.EXPIRED).count();
        
        long revenue = all.stream()
                .filter(s -> s.getStatus() != SubscriptionStatus.EXPIRED) // Simple logic: active/cancelled contributed revenue
                .mapToLong(s -> s.getBillingCycle() == BillingCycle.YEARLY ? YEARLY_PRICE_PAISE : MONTHLY_PRICE_PAISE)
                .sum();

        java.util.Map<String, Long> distribution = all.stream()
                .collect(java.util.stream.Collectors.groupingBy(s -> s.getBillingCycle().name(), java.util.stream.Collectors.counting()));

        return SubscriptionStats.builder()
                .totalActiveSubscriptions(active)
                .totalCancelledSubscriptions(cancelled)
                .totalExpiredSubscriptions(expired)
                .totalRevenueInPaise(revenue)
                .planDistribution(distribution)
                .build();
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
        
        if (subscriptionRepository.existsByRazorpayPaymentId(paymentId)) {
            return VerifyPaymentResponse.builder()
                    .success(false)
                    .message("This payment has already been processed.")
                    .build();
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        BillingCycle cycle = BillingCycle.valueOf(billingCycle.toUpperCase());
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = cycle == BillingCycle.YEARLY
                ? startDate.plusDays(365)
                : startDate.plusDays(30);

        Subscription subscription = Subscription.builder()
                .username(username)
                .plan(PlanType.PREMIUM)
                .billingCycle(cycle)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(startDate)
                .endDate(endDate)
                .razorpayOrderId(orderId)
                .razorpayPaymentId(paymentId)
                .build();
        subscriptionRepository.save(subscription);

        // Call Auth Service to update user plan
        var updatePlanResponse = authServiceClient.updatePlan(
                new AuthServiceClient.UpdatePlanRequest(username, PlanType.PREMIUM)
        );

        return VerifyPaymentResponse.builder()
                .success(true)
                .message("Welcome to Premium! Your plan is now active until " + endDate.toLocalDate())
                .newToken(updatePlanResponse.get("token"))
                .build();
    }
}
