package com.intermidateproject.fees.fees_service.controller;

import com.intermidateproject.fees.fees_service.anomaly.Anomaly;
import com.intermidateproject.fees.fees_service.dto.ExplainedAnomaly;
import com.intermidateproject.fees.fees_service.service.AiExplanationService;
import com.intermidateproject.fees.fees_service.service.AnomalyDetectionService;
import com.intermidateproject.fees.fees_service.service.AnomalyExplanationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/anomalies")
public class AnomalyController {

    private final AnomalyDetectionService anomalyDetectionService;
    private final AnomalyExplanationService anomalyExplanationService;
    private final AiExplanationService aiExplanationService;

    public AnomalyController(AnomalyDetectionService anomalyDetectionService,
                             AnomalyExplanationService anomalyExplanationService, AiExplanationService aiExplanationService) {
        this.anomalyDetectionService = anomalyDetectionService;
        this.anomalyExplanationService = anomalyExplanationService;
        this.aiExplanationService = aiExplanationService;
    }

    @GetMapping
    public ResponseEntity<List<Anomaly>> getAnomalies() {
        return ResponseEntity.ok(anomalyDetectionService.detectAnomalies());
    }

    @GetMapping("/explained")
    public ResponseEntity<List<ExplainedAnomaly>> getExplainedAnomalies() {
        List<ExplainedAnomaly> result = new ArrayList<>();
        for (Anomaly anomaly : anomalyDetectionService.detectAnomalies()) {
            result.add(new ExplainedAnomaly(
                    anomaly, anomalyExplanationService.explainWithDetails(anomaly)));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/explained-ai")
    public ResponseEntity<List<ExplainedAnomaly>> getAiExplainedAnomalies() {
        List<ExplainedAnomaly> result = new ArrayList<>();
        for (Anomaly anomaly : anomalyDetectionService.detectAnomalies()) {
            result.add(new ExplainedAnomaly(anomaly, aiExplanationService.explain(anomaly)));
        }
        return ResponseEntity.ok(result);
    }
}