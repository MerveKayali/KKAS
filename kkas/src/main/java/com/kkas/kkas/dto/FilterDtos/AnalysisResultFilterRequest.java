package com.kkas.kkas.dto.FilterDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnalysisResultFilterRequest {
    private String agentName;
    private String emotion;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int page = 0;
    private int size = 10;
    private String sortBy = "callDate";
    private String sortDirection = "DESC"; // ASC ya da DESC
}
