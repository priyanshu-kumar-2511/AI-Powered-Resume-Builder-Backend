package com.airesume.exportservice.client;

import com.airesume.exportservice.config.FeignAuthForwardingConfig;
import com.airesume.exportservice.dto.ResumeResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "RESUME-SERVICE", configuration = FeignAuthForwardingConfig.class)
public interface ResumeServiceClient {
    @GetMapping("/{resumeId}")
    ResumeResponseDTO getResumeById(@PathVariable("resumeId") Long resumeId);
}
