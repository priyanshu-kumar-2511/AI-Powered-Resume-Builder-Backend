package com.airesume.resumeservice.client;

import com.airesume.resumeservice.client.dto.SectionPayload;
import com.airesume.resumeservice.config.FeignAuthForwardingConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "section-service", configuration = FeignAuthForwardingConfig.class)
public interface SectionServiceClient {

    @GetMapping("/resume/{resumeId}")
    List<SectionPayload> getSectionsByResume(@PathVariable Long resumeId);

    @PostMapping
    SectionPayload addSection(@RequestBody SectionPayload payload);

    @DeleteMapping("/resume/{resumeId}/all")
    void deleteAllSectionsByResume(@PathVariable Long resumeId);
}
