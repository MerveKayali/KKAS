package com.kkas.kkas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResultRequestDTO {
    @NotBlank
    private String agentName;

    @NotBlank
    private String topic;

    @NotBlank
    private String audioFilePath;

    private LocalDateTime callDate;

    private String emotion;

    private Double qualityScore;

    private String summary;



}
