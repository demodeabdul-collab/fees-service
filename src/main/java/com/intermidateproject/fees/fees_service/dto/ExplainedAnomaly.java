package com.intermidateproject.fees.fees_service.dto;

import com.intermidateproject.fees.fees_service.anomaly.Anomaly;

public class ExplainedAnomaly {
    private final Anomaly anomaly;
    private final String explanation;

    public ExplainedAnomaly(Anomaly anomaly, String explanation) {
        this.anomaly = anomaly;
        this.explanation = explanation;
    }

    public Anomaly getAnomaly() {
        return anomaly;
    }

    public String getExplanation() {
        return explanation;
    }
}