package com.intermidateproject.fees.fees_service.controller;

import com.intermidateproject.fees.fees_service.util.HmacUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController
@RequestMapping("/api/simulator")
public class SimulatorController {

    private final String webhookSecret;

    public SimulatorController(@Value("${webhook.secret}") String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/sign")
    public ResponseEntity<String> signPayload(@RequestBody String rawBody) {
        String signature = HmacUtil.computeHmacSha256(rawBody, webhookSecret);
        return ResponseEntity.ok(signature);
    }
}