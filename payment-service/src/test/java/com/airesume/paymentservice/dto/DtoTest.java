package com.airesume.paymentservice.dto;

import com.airesume.paymentservice.model.PlanType;
import com.airesume.paymentservice.model.SubscriptionStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    void testCreateOrderRequest() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setBillingCycle("MONTHLY");
        assertEquals("MONTHLY", req.getBillingCycle());
        
        CreateOrderRequest req2 = new CreateOrderRequest("YEARLY");
        assertEquals("YEARLY", req2.getBillingCycle());
    }

    @Test
    void testCreateOrderResponse() {
        CreateOrderResponse res = CreateOrderResponse.builder()
                .orderId("ord")
                .amountInPaise(10000L)
                .currency("INR")
                .keyId("key")
                .billingCycle("MONTHLY")
                .build();
        
        assertEquals("ord", res.getOrderId());
        assertEquals(10000L, res.getAmountInPaise());
        assertEquals("INR", res.getCurrency());
        assertEquals("key", res.getKeyId());
        assertEquals("MONTHLY", res.getBillingCycle());

        CreateOrderResponse res2 = new CreateOrderResponse();
        res2.setOrderId("ord2");
        assertEquals("ord2", res2.getOrderId());
        
        CreateOrderResponse res3 = new CreateOrderResponse("o", 1L, "c", "k", "b");
        assertEquals("o", res3.getOrderId());
    }

    @Test
    void testVerifyPaymentRequest() {
        VerifyPaymentRequest req = new VerifyPaymentRequest();
        req.setRazorpayOrderId("ord");
        req.setRazorpayPaymentId("pay");
        req.setRazorpaySignature("sig");

        assertEquals("ord", req.getRazorpayOrderId());
        assertEquals("pay", req.getRazorpayPaymentId());
        assertEquals("sig", req.getRazorpaySignature());
    }

    @Test
    void testVerifyPaymentResponse() {
        VerifyPaymentResponse res = VerifyPaymentResponse.builder()
                .success(true)
                .message("msg")
                .newToken("token")
                .build();
        
        assertTrue(res.isSuccess());
        assertEquals("msg", res.getMessage());
        assertEquals("token", res.getNewToken());

        VerifyPaymentResponse res2 = new VerifyPaymentResponse();
        res2.setSuccess(false);
        assertFalse(res2.isSuccess());

        VerifyPaymentResponse res3 = new VerifyPaymentResponse(true, "m", "t");
        assertTrue(res3.isSuccess());
    }

    @Test
    void testSubscriptionStatusResponse() {
        SubscriptionStatusResponse res = SubscriptionStatusResponse.builder()
                .plan(PlanType.PREMIUM)
                .status(SubscriptionStatus.ACTIVE)
                .build();
        
        assertEquals(PlanType.PREMIUM, res.getPlan());
        assertEquals(SubscriptionStatus.ACTIVE, res.getStatus());
        
        SubscriptionStatusResponse res2 = new SubscriptionStatusResponse();
        res2.setPlan(PlanType.FREE);
        assertEquals(PlanType.FREE, res2.getPlan());
    }

    @Test
    void testSubscriptionModel() {
        com.airesume.paymentservice.model.Subscription sub = com.airesume.paymentservice.model.Subscription.builder()
                .id(1L)
                .username("user1")
                .plan(PlanType.PREMIUM)
                .status(SubscriptionStatus.ACTIVE)
                .build();
        
        assertEquals(1L, sub.getId());
        assertEquals("user1", sub.getUsername());
        assertEquals(PlanType.PREMIUM, sub.getPlan());
        assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
        assertEquals("user1", sub.getUsernameManual());
        try {
            java.lang.reflect.Method onCreateMethod = com.airesume.paymentservice.model.Subscription.class.getDeclaredMethod("onCreate");
            onCreateMethod.setAccessible(true);
            onCreateMethod.invoke(sub);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertNotNull(sub.getCreatedAt());

        sub.setRazorpayPaymentId("pay_123");
        assertEquals("pay_123", sub.getRazorpayPaymentId());
        
        assertNotNull(sub.toString());
    }

    @Test
    void testEnums() {
        assertEquals("MONTHLY", com.airesume.paymentservice.model.BillingCycle.MONTHLY.name());
        assertEquals("FREE", PlanType.FREE.name());
        assertEquals("ACTIVE", SubscriptionStatus.ACTIVE.name());
    }
}
