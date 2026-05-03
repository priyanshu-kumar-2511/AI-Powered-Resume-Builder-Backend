package com.airesume.exportservice.client;

import com.airesume.exportservice.dto.TemplateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "TEMPLATE-SERVICE")
public interface TemplateServiceClient {
    @GetMapping("/{templateId}")
    TemplateDTO getTemplateById(@PathVariable("templateId") Long templateId);
}
