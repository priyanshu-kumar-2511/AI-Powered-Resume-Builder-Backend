package com.airesume.paymentservice.service;

import com.airesume.paymentservice.client.AuthServiceClient;
import com.airesume.paymentservice.dto.SubscriptionStats;
import com.airesume.paymentservice.dto.VerifyPaymentRequest;
import com.airesume.paymentservice.dto.VerifyPaymentResponse;
import com.airesume.paymentservice.model.BillingCycle;
import com.airesume.paymentservice.model.PlanType;
import com.airesume.paymentservice.model.Subscription;
import com.airesume.paymentservice.model.SubscriptionStatus;
import com.airesume.paymentservice.repository.SubscriptionRepository;
import com.razorpay.Order;
import com.razorpay.OrderClient;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "razorpayKeyId", "rzp_test_123");
        ReflectionTestUtils.setField(paymentService, "razorpayKeySecret", "secret");

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getName()).thenReturn("testuser");
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // ── createOrder ──────────────────────────────────────────────────────────

    @Test
    void testCreateOrder_Monthly() {
        try (MockedConstruction<RazorpayClient> mocked = mockConstruction(RazorpayClient.class, (mock, context) -> {
            mock.orders = mock(OrderClient.class);
            Order order = mock(Order.class);
            when(order.get("id")).thenReturn("order_123");
            when(mock.orders.create(any(JSONObject.class))).thenReturn(order);
        })) {
            var response = paymentService.createOrder("MONTHLY");
            assertEquals("order_123", response.getOrderId());
            assertEquals(5000L, response.getAmountInPaise());
            assertEquals("MONTHLY", response.getBillingCycle());
            assertEquals("INR", response.getCurrency());
        }
    }

    @Test
    void testCreateOrder_Yearly() {
        try (MockedConstruction<RazorpayClient> mocked = mockConstruction(RazorpayClient.class, (mock, context) -> {
            mock.orders = mock(OrderClient.class);
            Order order = mock(Order.class);
            when(order.get("id")).thenReturn("order_yearly_123");
            when(mock.orders.create(any(JSONObject.class))).thenReturn(order);
        })) {
            var response = paymentService.createOrder("YEARLY");
            assertEquals("order_yearly_123", response.getOrderId());
            assertEquals(50000L, response.getAmountInPaise());
        }
    }

    @Test
    void testCreateOrder_Failure() {
        try (MockedConstruction<RazorpayClient> mocked = mockConstruction(RazorpayClient.class, (mock, context) -> {
            mock.orders = mock(OrderClient.class);
            when(mock.orders.create(any())).thenThrow(new RazorpayException("Razorpay failure"));
        })) {
            assertThrows(RuntimeException.class, () -> paymentService.createOrder("MONTHLY"));
        }
    }

    // ── verifyAndActivate ─────────────────────────────────────────────────────

    @Test
    void testVerifyAndActivate_InvalidSignature_ReturnsFalse() {
        VerifyPaymentRequest request = VerifyPaymentRequest.builder()
                .razorpayOrderId("order_abc")
                .razorpayPaymentId("pay_abc")
                .razorpaySignature("wrong_signature")
                .billingCycle("MONTHLY")
                .build();

        VerifyPaymentResponse response = paymentService.verifyAndActivate(request);

        assertFalse(response.isSuccess());
        assertEquals("Payment verification failed. Invalid signature.", response.getMessage());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void testVerifyAndActivate_DuplicatePayment() {
        VerifyPaymentRequest request = VerifyPaymentRequest.builder()
                .razorpayOrderId("order_123")
                .razorpayPaymentId("pay_123")
                .razorpaySignature("13f113268a0357923e6390e6773754dc39c991f05a999bcaf04c161c59aeaaf8")
                .billingCycle("MONTHLY")
                .build();

        when(subscriptionRepository.existsByRazorpayPaymentId("pay_123")).thenReturn(true);

        VerifyPaymentResponse response = paymentService.verifyAndActivate(request);

        assertFalse(response.isSuccess());
        assertEquals("This payment has already been processed.", response.getMessage());
    }

    @Test
    void testVerifyAndActivate_Monthly_Success() {
        String signature = "13f113268a0357923e6390e6773754dc39c991f05a999bcaf04c161c59aeaaf8";
        VerifyPaymentRequest request = VerifyPaymentRequest.builder()
                .razorpayOrderId("order_123")
                .razorpayPaymentId("pay_123")
                .razorpaySignature(signature)
                .billingCycle("MONTHLY")
                .build();

        when(subscriptionRepository.existsByRazorpayPaymentId("pay_123")).thenReturn(false);
        when(authServiceClient.updatePlan(any())).thenReturn(Map.of("token", "new_token"));

        VerifyPaymentResponse response = paymentService.verifyAndActivate(request);

        assertTrue(response.isSuccess());
        assertEquals("new_token", response.getNewToken());
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void testVerifyAndActivate_Yearly_Success() {
        String signature = "13f113268a0357923e6390e6773754dc39c991f05a999bcaf04c161c59aeaaf8";
        VerifyPaymentRequest request = VerifyPaymentRequest.builder()
                .razorpayOrderId("order_123")
                .razorpayPaymentId("pay_123")
                .razorpaySignature(signature)
                .billingCycle("YEARLY")
                .build();

        when(subscriptionRepository.existsByRazorpayPaymentId("pay_123")).thenReturn(false);
        when(authServiceClient.updatePlan(any())).thenReturn(Map.of("token", "new_token_yearly"));

        VerifyPaymentResponse response = paymentService.verifyAndActivate(request);

        assertTrue(response.isSuccess());
        assertEquals("new_token_yearly", response.getNewToken());
    }

    @Test
    void testIsValidSignature_NullOrderId_ThrowsAndReturnsFalse() {
        VerifyPaymentRequest request = VerifyPaymentRequest.builder()
                .razorpayOrderId(null)
                .razorpayPaymentId("pay_123")
                .razorpaySignature("sig")
                .billingCycle("MONTHLY")
                .build();

        VerifyPaymentResponse response = paymentService.verifyAndActivate(request);
        assertFalse(response.isSuccess());
    }

    @Test
    void testIsValidSignature_NullSecret_ThrowsAndReturnsFalse() {
        ReflectionTestUtils.setField(paymentService, "razorpayKeySecret", null);
        VerifyPaymentRequest request = VerifyPaymentRequest.builder()
                .razorpayOrderId("order_123")
                .razorpayPaymentId("pay_123")
                .razorpaySignature("sig")
                .billingCycle("MONTHLY")
                .build();

        VerifyPaymentResponse response = paymentService.verifyAndActivate(request);
        assertFalse(response.isSuccess());
    }

    // ── completeDevPayment ────────────────────────────────────────────────────

    @Test
    void testCompleteDevPayment_NotAllowed_LiveKey() {
        ReflectionTestUtils.setField(paymentService, "razorpayKeyId", "rzp_live_123");
        assertThrows(RuntimeException.class, () -> paymentService.completeDevPayment("MONTHLY"));
    }

    @Test
    void testCompleteDevPayment_Monthly_Success() {
        when(subscriptionRepository.existsByRazorpayPaymentId(anyString())).thenReturn(false);
        when(authServiceClient.updatePlan(any())).thenReturn(Map.of("token", "new_token_monthly"));

        VerifyPaymentResponse response = paymentService.completeDevPayment("MONTHLY");

        assertTrue(response.isSuccess());
        assertEquals("new_token_monthly", response.getNewToken());
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void testCompleteDevPayment_Yearly_Success() {
        when(subscriptionRepository.existsByRazorpayPaymentId(anyString())).thenReturn(false);
        when(authServiceClient.updatePlan(any())).thenReturn(Map.of("token", "new_token"));

        VerifyPaymentResponse response = paymentService.completeDevPayment("YEARLY");

        assertTrue(response.isSuccess());
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void testCompleteDevPayment_Duplicate() {
        when(subscriptionRepository.existsByRazorpayPaymentId(anyString())).thenReturn(true);

        VerifyPaymentResponse response = paymentService.completeDevPayment("MONTHLY");

        assertFalse(response.isSuccess());
        assertEquals("This payment has already been processed.", response.getMessage());
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    // ── getStatus ─────────────────────────────────────────────────────────────

    @Test
    void testGetStatus_FreeUser_NoSubscription() {
        when(subscriptionRepository.findTopByUsernameAndStatusOrderByStartDateDesc(anyString(), any()))
                .thenReturn(Optional.empty());

        var response = paymentService.getStatus("testuser");

        assertEquals(PlanType.FREE, response.getPlan());
    }

    @Test
    void testGetStatus_PremiumUser_AllFields() {
        Subscription subscription = Subscription.builder()
                .plan(PlanType.PREMIUM)
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDateTime.now().minusDays(5))
                .endDate(LocalDateTime.now().plusDays(25))
                .razorpayPaymentId("pay_xyz")
                .build();

        when(subscriptionRepository.findTopByUsernameAndStatusOrderByStartDateDesc(anyString(), any()))
                .thenReturn(Optional.of(subscription));

        var response = paymentService.getStatus("testuser");

        assertEquals(PlanType.PREMIUM, response.getPlan());
        assertEquals(SubscriptionStatus.ACTIVE, response.getStatus());
        assertEquals(BillingCycle.MONTHLY, response.getBillingCycle());
        assertEquals("pay_xyz", response.getRazorpayPaymentId());
        assertNotNull(response.getStartDate());
        assertNotNull(response.getEndDate());
    }

    // ── cancelSubscription ────────────────────────────────────────────────────

    @Test
    void testCancelSubscription_Success() {
        Subscription subscription = Subscription.builder()
                .username("testuser")
                .status(SubscriptionStatus.ACTIVE)
                .build();

        when(subscriptionRepository.findTopByUsernameAndStatusOrderByStartDateDesc(anyString(), any()))
                .thenReturn(Optional.of(subscription));

        String result = paymentService.cancelSubscription("testuser");

        assertEquals("Subscription cancelled successfully.", result);
        assertEquals(SubscriptionStatus.CANCELLED, subscription.getStatus());
        verify(subscriptionRepository).save(subscription);
        verify(authServiceClient).updatePlan(any());
    }

    @Test
    void testCancelSubscription_NotFound() {
        when(subscriptionRepository.findTopByUsernameAndStatusOrderByStartDateDesc(anyString(), any()))
                .thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> paymentService.cancelSubscription("user1"));
    }

    // ── getStats ──────────────────────────────────────────────────────────────

    @Test
    void testGetStats_Empty() {
        when(subscriptionRepository.findAll()).thenReturn(List.of());

        SubscriptionStats stats = paymentService.getStats();

        assertEquals(0L, stats.getTotalActiveSubscriptions());
        assertEquals(0L, stats.getTotalCancelledSubscriptions());
        assertEquals(0L, stats.getTotalExpiredSubscriptions());
        assertEquals(0L, stats.getTotalRevenueInPaise());
        assertTrue(stats.getPlanDistribution().isEmpty());
    }

    @Test
    void testGetStats_WithMixedSubscriptions() {
        Subscription active = Subscription.builder()
                .status(SubscriptionStatus.ACTIVE)
                .billingCycle(BillingCycle.MONTHLY)
                .build();
        Subscription cancelled = Subscription.builder()
                .status(SubscriptionStatus.CANCELLED)
                .billingCycle(BillingCycle.YEARLY)
                .build();
        Subscription expired = Subscription.builder()
                .status(SubscriptionStatus.EXPIRED)
                .billingCycle(BillingCycle.MONTHLY)
                .build();

        when(subscriptionRepository.findAll()).thenReturn(List.of(active, cancelled, expired));

        SubscriptionStats stats = paymentService.getStats();

        assertEquals(1L, stats.getTotalActiveSubscriptions());
        assertEquals(1L, stats.getTotalCancelledSubscriptions());
        assertEquals(1L, stats.getTotalExpiredSubscriptions());
        // Revenue: active(MONTHLY=5000) + cancelled(YEARLY=50000). Expired excluded.
        assertEquals(55000L, stats.getTotalRevenueInPaise());
        assertEquals(2L, stats.getPlanDistribution().get("MONTHLY")); // active + expired
        assertEquals(1L, stats.getPlanDistribution().get("YEARLY"));  // cancelled
    }

    @Test
    void testGetStats_AllActiveYearly() {
        Subscription s1 = Subscription.builder()
                .status(SubscriptionStatus.ACTIVE)
                .billingCycle(BillingCycle.YEARLY)
                .build();
        Subscription s2 = Subscription.builder()
                .status(SubscriptionStatus.ACTIVE)
                .billingCycle(BillingCycle.YEARLY)
                .build();

        when(subscriptionRepository.findAll()).thenReturn(List.of(s1, s2));

        SubscriptionStats stats = paymentService.getStats();

        assertEquals(2L, stats.getTotalActiveSubscriptions());
        assertEquals(100000L, stats.getTotalRevenueInPaise()); // 2 * 50000
    }

    // ── getSubscriptions ──────────────────────────────────────────────────────

    @Test
    void testGetSubscriptions_ReturnsPage() {
        Subscription sub = Subscription.builder()
                .username("user1")
                .plan(PlanType.PREMIUM)
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDateTime.now().minusDays(10))
                .endDate(LocalDateTime.now().plusDays(20))
                .razorpayOrderId("order_1")
                .razorpayPaymentId("pay_1")
                .build();

        Page<Subscription> page = new PageImpl<>(List.of(sub));
        when(subscriptionRepository.findAll(any(PageRequest.class))).thenReturn(page);

        var result = paymentService.getSubscriptions(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        var item = result.getContent().get(0);
        assertEquals("user1", item.getUsername());
        assertEquals("user1", item.getFullName()); // fallback to username
        assertEquals(PlanType.PREMIUM, item.getPlan());
        assertEquals(SubscriptionStatus.ACTIVE, item.getStatus());
        assertEquals("order_1", item.getRazorpayOrderId());
        assertEquals("pay_1", item.getRazorpayPaymentId());
    }

    @Test
    void testGetSubscriptions_Empty() {
        Page<Subscription> emptyPage = new PageImpl<>(List.of());
        when(subscriptionRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

        var result = paymentService.getSubscriptions(PageRequest.of(0, 10));

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }
}
