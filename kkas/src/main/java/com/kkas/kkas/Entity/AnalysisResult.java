package com.kkas.kkas.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="analysis_result")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String agentName;

    private String emotion;

    private String topic;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private Double qualityScore;

    private LocalDateTime callDate;

    private String audioFilePath;



}
