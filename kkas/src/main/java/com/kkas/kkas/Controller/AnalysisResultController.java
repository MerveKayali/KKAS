package com.kkas.kkas.Controller;

import com.kkas.kkas.Service.AnalysisResultService;
import com.kkas.kkas.dto.AnalysisResultRequestDTO;
import com.kkas.kkas.dto.AnalysisResultResponseDTO;
import com.kkas.kkas.dto.DashboardDTOs.DashboardSummaryResponse;
import com.kkas.kkas.dto.FilterDtos.AnalysisResultFilterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/analysis-result")
@RequiredArgsConstructor
public class AnalysisResultController {

    private final AnalysisResultService service;

    // Manuel veri kaydı (önceden analiz edilmiş veriler için)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalysisResultResponseDTO> save(@RequestBody AnalysisResultRequestDTO request) {
        return ResponseEntity.ok(service.save(request));
    }

    // Tüm verileri getir
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<AnalysisResultResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // ID ile detay getir
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<AnalysisResultResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // Güncelleme
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalysisResultResponseDTO> update(@PathVariable UUID id, @RequestBody AnalysisResultRequestDTO request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    // Silme
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Filtreleme
    @PostMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Page<AnalysisResultResponseDTO> getFilteredResults(@RequestBody AnalysisResultFilterRequest filterRequest) {
        return service.getFilteredResults(filterRequest);
    }

    // Dashboard özet
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
        DashboardSummaryResponse summary = service.getDahsboardSummary();
        return ResponseEntity.ok(summary);
    }

    // Ses dosyası yükleyerek analiz yap
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalysisResultResponseDTO> analyzeAudio(
            @RequestParam("agentName") String agentName,
            @RequestParam("file") MultipartFile file) {
        AnalysisResultResponseDTO response = service.processAndSaveAnalysis(agentName, file);
        return ResponseEntity.ok(response);
    }
}
