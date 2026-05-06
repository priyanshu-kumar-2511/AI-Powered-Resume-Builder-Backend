package com.airesume.paymentservice.client;

import com.airesume.paymentservice.model.PlanType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "AUTH-SERVICE")
public interface AuthServiceClient {

    @PostMapping("/api/v1/internal/users/update-plan")
    Map<String, String> updatePlan(@RequestBody UpdatePlanRequest request);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class UpdatePlanRequest {
        private String username;
        private PlanType plan;
    }
}
