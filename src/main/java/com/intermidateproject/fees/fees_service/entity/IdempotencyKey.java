package com.intermidateproject.fees.fees_service.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKey {

    @Id
    @Column(name = "idempotency_key")
    private String key;

    @Column(name= "request_hash", nullable = false)
    private String requestHash;

    @Column(name = "response_body")
    private String responseBody;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "created_at", nullable = false,updatable = false)
    private LocalDateTime createdAt;

    protected IdempotencyKey() {}

    public IdempotencyKey(String key, String requestHash) {
        this.key = key;
        this.requestHash = requestHash;
    }
    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public String getKey() {return key;}
    public String getRequestHash() {return requestHash;}
    public String getResponseBody() {return responseBody;}
    public LocalDateTime getCreatedAt() {return createdAt;}
    public Integer getResponseStatus() {return responseStatus;}

    public void setResponseStatus(Integer requestStatus){this.responseStatus =requestStatus;}
    public void setResponseBody(String requestBody) {this.responseBody = requestBody;}






}
