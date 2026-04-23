package com.airesume.resumeservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for internal communication to update the ATS score.
 * This is primarily used by the ai-service.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtsUpdateDTO {
    
    /**
     * The new ATS compatibility score.
     */
    @NotNull(message = "ATS score is required")
    @Min(value = 0, message = "Score cannot be less than 0")
    @Max(value = 100, message = "Score cannot exceed 100")
    private Integer atsScore;
}
