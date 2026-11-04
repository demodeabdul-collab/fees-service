package com.intermidateproject.fees.fees_service.service;

import com.intermidateproject.fees.fees_service.anomaly.Anomaly;
import org.springframework.stereotype.Service;

@Service
public class AnomalyExplanationService {

    public String explain(Anomaly anomaly) {
        return switch (anomaly.getType()) {
            case STATUS_MISMATCH ->
                    "The invoice's status label does not match what has actually been paid. "
                            + "Reports and student portals may show the wrong amount owed. "
                            + "Next step: review the invoice's payment history and correct the status.";
            case NEGATIVE_BALANCE ->
                    "More money has been recorded against this invoice than it was ever billed for. "
                            + "The student may be owed a refund, or a payment may have been recorded against the wrong invoice. "
                            + "Next step: check the payments on this invoice, then refund or move the excess.";
            case SUSPECTED_DUPLICATE ->
                    "Two payments of the same amount were recorded on the same invoice within a few minutes, "
                            + "under different references. This may be an accidental double payment, "
                            + "or it may be two legitimate installments. "
                            + "Next step: compare both references with the bank statement and reverse one if it is a duplicate.";
        };
    }

    public String explainWithDetails(Anomaly anomaly) {
        return explain(anomaly) + " Details: " + anomaly.getDetails();
    }
}