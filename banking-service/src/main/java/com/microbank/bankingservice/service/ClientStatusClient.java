package com.microbank.bankingservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Component
public class ClientStatusClient {

  private final RestClient rest;
  private final String apiKey;

  public ClientStatusClient(@Value("${client-service.baseUrl}") String baseUrl,
                            @Value("${client-service.apiKey}") String apiKey) {
    this.rest = RestClient.builder().baseUrl(baseUrl).build();
    this.apiKey = apiKey;
  }

  public boolean isBlacklisted(UUID clientId) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-API-KEY", apiKey);
    Map body = rest.get()
        .uri("/api/internal/clients/{id}/status", clientId.toString())
        .headers(h -> h.addAll(headers))
        .retrieve()
        .body(Map.class);
    Object v = body.get("blacklisted");
    return v instanceof Boolean b && b;
  }
}
