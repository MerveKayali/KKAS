package com.kkas.kkas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResultResponseDTO {
    private UUID id;

    private String agentName;

    private String emotion;

    private String topic;

    private String summary;

    private Double qualityScore;

    private LocalDateTime callDate;

    private String audioFilePath;


}
