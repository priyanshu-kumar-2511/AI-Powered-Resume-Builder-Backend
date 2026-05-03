package com.airesume.exportservice.client;

import com.airesume.exportservice.config.FeignAuthForwardingConfig;
import com.airesume.exportservice.dto.SectionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "SECTION-SERVICE", configuration = FeignAuthForwardingConfig.class)
public interface SectionServiceClient {
    @GetMapping("/resume/{resumeId}")
    List<SectionDTO> getSectionsByResume(@PathVariable("resumeId") Long resumeId);
}
