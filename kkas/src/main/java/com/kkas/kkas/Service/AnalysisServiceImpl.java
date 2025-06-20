package com.kkas.kkas.Service;

import com.kkas.kkas.Entity.AnalysisResult;
import com.kkas.kkas.Repository.AnalysisResultRepository;
import com.kkas.kkas.dto.AnalysisResultRequestDTO;
import com.kkas.kkas.dto.AnalysisResultResponseDTO;
import com.kkas.kkas.dto.DashboardDTOs.DashboardSummaryResponse;
import com.kkas.kkas.dto.FilterDtos.AnalysisResultFilterRequest;
import com.kkas.kkas.dto.NLP.NlpAnalysisResponseDTO;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisResultService {

    private final AnalysisResultRepository repository;
    private final FileStorageService fileStorageService;

    @Override
    public AnalysisResultResponseDTO save(AnalysisResultRequestDTO request) {
        AnalysisResult entity = buildEntityFromRequest(request);
        return buildResponseFromEntity(repository.save(entity));
    }

    @Override
    public List<AnalysisResultResponseDTO> getAll() {
        return repository.findAll()
                .stream()
                .map(this::buildResponseFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public AnalysisResultResponseDTO getById(UUID id) {
        AnalysisResult entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("AnalysisResult not found with id: " + id));
        return buildResponseFromEntity(entity);
    }

    @Override
    public AnalysisResultResponseDTO update(UUID id, AnalysisResultRequestDTO request) {
        AnalysisResult entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Analysis Result Not Found with id: " + id));

        entity.setAgentName(request.getAgentName());
        entity.setEmotion(request.getEmotion());
        entity.setTopic(request.getTopic());
        entity.setCallDate(request.getCallDate());
        entity.setQualityScore(request.getQualityScore());
        entity.setAudioFilePath(request.getAudioFilePath());

        AnalysisResult updated = repository.save(entity);
        return buildResponseFromEntity(updated);
    }

    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("AnalysisResult not found with id: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public Page<AnalysisResultResponseDTO> getFilteredResults(AnalysisResultFilterRequest filterRequest) {
        Pageable pageable = PageRequest.of(
                filterRequest.getPage(),
                filterRequest.getSize(),
                Sort.by(Sort.Direction.fromString(filterRequest.getSortDirection()), filterRequest.getSortBy())
        );

        Page<AnalysisResult> page = repository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filterRequest.getAgentName() != null) {
                predicates.add(cb.equal(root.get("agentName"), filterRequest.getAgentName()));
            }

            if (filterRequest.getEmotion() != null) {
                predicates.add(cb.equal(root.get("emotion"), filterRequest.getEmotion()));
            }

            if (filterRequest.getStartDate() != null && filterRequest.getEndDate() != null) {
                predicates.add(cb.between(root.get("callDate"), filterRequest.getStartDate(), filterRequest.getEndDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        return page.map(this::buildResponseFromEntity);
    }

    @Override
    public DashboardSummaryResponse getDahsboardSummary() {
        List<AnalysisResult> results = repository.findAll();

        long totalCallCount = results.size();

        double averageQualityScore = results.stream()
                .filter(r -> r.getQualityScore() != null)
                .mapToDouble(AnalysisResult::getQualityScore)
                .average()
                .orElse(0.0);

        Map<String, Long> callCountByAgent = results.stream()
                .collect(Collectors.groupingBy(AnalysisResult::getAgentName, Collectors.counting()));

        Map<String, Long> emotionDistribution = results.stream()
                .collect(Collectors.groupingBy(AnalysisResult::getEmotion, Collectors.counting()));

        Map<String, Long> topicDistribution = results.stream()
                .collect(Collectors.groupingBy(AnalysisResult::getTopic, Collectors.counting()));

        return DashboardSummaryResponse.builder()
                .totalCallCount(totalCallCount)
                .averageQualityScore(averageQualityScore)
                .callCountByAgent(callCountByAgent)
                .emotionDistribution(emotionDistribution)
                .topicDistribution(topicDistribution)
                .build();
    }

    @Override
    public AnalysisResultResponseDTO processAndSaveAnalysis(String agentName, MultipartFile file) {
        try {
            // 1. Dosyayı uploads/ klasörüne kaydet
            String savedFilePath = fileStorageService.saveFile(file);

            // 2. Python servisine kaydedilen path'i ver
            NlpAnalysisResponseDTO analysis = analyzeAudioWithPython(savedFilePath);

            // 3. Puan hesapla (calculateScore metoduna göre)
            double score = calculateScore(analysis.getSentiment(), analysis.getTopic());

            // 4. Entity oluştur ve kaydet
            AnalysisResult entity = AnalysisResult.builder()
                    .agentName(agentName)
                    .emotion(analysis.getSentiment())
                    .topic(analysis.getTopic())
                    .callDate(LocalDateTime.now())
                    .audioFilePath(savedFilePath)
                    .qualityScore(score)
                    .summary(analysis.getSummary())
                    .build();

            AnalysisResult saved = repository.save(entity);
            return buildResponseFromEntity(saved);

        } catch (IOException e) {
            throw new RuntimeException("Ses dosyası işlenirken hata oluştu", e);
        }
    }

    private double calculateScore(String sentiment, String topic) {
        double score = 100.0;
        switch (sentiment.toLowerCase())
        {
            case "positive" -> score -= 0;  // olumlu
            case "neutral" -> score -= 5;   // etkisiz
            case "negative" -> score -= 20; // olumsuz
            default -> score -= 10;         // bilinmeyen
        }
        // Topic'e göre ceza
        switch (topic.toLowerCase()) {
            case "iade", "return request" -> score -= 15;
            case "teknik destek", "technical issue" -> score -= 10;
            case "ürün arızası", "product defect" -> score -= 20;
            case "kargo gecikmesi", "shipping delay" -> score -= 10;
            case "şikayet", "complaint" -> score -= 20;
            case "memnuniyet", "satisfaction" -> score-= 0;
            default -> score -= 5; // bilinmeyen topic
        }
        return Math.max(0, Math.min(100, score));
    }

    private NlpAnalysisResponseDTO analyzeAudioWithPython(String audioFilePath) {
        String url = "http://localhost:8000/analyze";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(audioFilePath));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<NlpAnalysisResponseDTO> response = restTemplate.postForEntity(
                url,
                requestEntity,
                NlpAnalysisResponseDTO.class
        );

        return response.getBody();
    }

    private AnalysisResult buildEntityFromRequest(AnalysisResultRequestDTO request) {
        return AnalysisResult.builder()
                .agentName(request.getAgentName())
                .emotion(request.getEmotion())
                .topic(request.getTopic())
                .qualityScore(request.getQualityScore())
                .callDate(request.getCallDate())
                .audioFilePath(request.getAudioFilePath())
                .summary(request.getSummary())
                .build();
    }

    private AnalysisResultResponseDTO buildResponseFromEntity(AnalysisResult entity) {
        return AnalysisResultResponseDTO.builder()
                .id(entity.getId())
                .agentName(entity.getAgentName())
                .emotion(entity.getEmotion())
                .summary(entity.getSummary())
                .topic(entity.getTopic())
                .qualityScore(entity.getQualityScore())
                .callDate(entity.getCallDate())
                .audioFilePath(entity.getAudioFilePath())
                .build();
    }
}
