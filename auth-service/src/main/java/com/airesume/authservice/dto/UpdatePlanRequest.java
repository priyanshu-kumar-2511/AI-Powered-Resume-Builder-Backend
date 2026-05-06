package com.airesume.authservice.dto;

import com.airesume.authservice.model.PlanType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePlanRequest {
    private String username;
    private PlanType plan;
}
