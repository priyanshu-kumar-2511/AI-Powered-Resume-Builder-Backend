package com.airesume.exportservice.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class ExportStatsDTO {
    private Long userId;
    private long totalExports;
    private long todayPdfCount;
    private Map<String, Long> countByFormat;
}
