package com.kkas.kkas.dto.NLP;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NlpAnalysisResponseDTO {
    private String transcript;
    private String sentiment;
    private String topic;
    private String summary;
}
