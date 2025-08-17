package com.microbank.clientservice.controller;

import com.microbank.clientservice.model.User;
import com.microbank.clientservice.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalController {

  private final UserRepository users;

  @Value("${client-service.apiKey}")
  private String apiKey;

  @GetMapping("/clients/{id}/status")
  public Map<String, Object> getStatus(@RequestHeader("X-API-KEY") String providedKey, @PathVariable UUID id) {
    if (!apiKey.equals(providedKey)) {
      throw new UnauthorizedException();
    }
    User u = users.findById(id).orElseThrow();
    return Map.of(
        "clientId", u.getId().toString(),
        "email", u.getEmail(),
        "blacklisted", u.isBlacklisted()
    );
  }

  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  private static class UnauthorizedException extends RuntimeException {}
}
