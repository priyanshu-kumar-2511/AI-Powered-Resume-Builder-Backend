package com.airesume.sectionservice.client;

import com.airesume.sectionservice.config.FeignAuthForwardingConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign Client for communication with the Resume Service.
 * Primarily used to verify resume existence and ownership before 
 * performing section-level modifications.
 */
@FeignClient(name = "resume-service", configuration = FeignAuthForwardingConfig.class)
public interface ResumeServiceClient {

    @GetMapping("/{resumeId}")
    Object getResumeById(@PathVariable Long resumeId);
}
