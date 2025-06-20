package com.kkas.kkas.dto.DashboardDTOs;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardSummaryResponse {
    private long totalCallCount;
    private Double averageQualityScore;
    private Map<String, Long>  callCountByAgent;
    private Map<String, Long> topicDistribution;
    private Map<String, Long> emotionDistribution;
}
