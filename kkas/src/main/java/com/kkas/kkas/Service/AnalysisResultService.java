package com.kkas.kkas.Service;

import com.kkas.kkas.Entity.AnalysisResult;
import com.kkas.kkas.dto.AnalysisResultRequestDTO;
import com.kkas.kkas.dto.AnalysisResultResponseDTO;
import com.kkas.kkas.dto.DashboardDTOs.DashboardSummaryResponse;
import com.kkas.kkas.dto.FilterDtos.AnalysisResultFilterRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface AnalysisResultService {
    AnalysisResultResponseDTO save(AnalysisResultRequestDTO request);
    List<AnalysisResultResponseDTO> getAll();
    AnalysisResultResponseDTO getById(UUID id);
    AnalysisResultResponseDTO update(UUID id, AnalysisResultRequestDTO request);
    void delete(UUID id);
    Page<AnalysisResultResponseDTO> getFilteredResults(AnalysisResultFilterRequest filterRequest);
    DashboardSummaryResponse getDahsboardSummary();
    AnalysisResultResponseDTO processAndSaveAnalysis(String agentName, MultipartFile file);
}
