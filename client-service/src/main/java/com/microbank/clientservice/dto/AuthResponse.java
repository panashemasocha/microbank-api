package com.microbank.clientservice.dto;

import java.util.Set;
import java.util.UUID;

public record AuthResponse(String token, UUID clientId, String email, String fullName, Set<String> roles, boolean blacklisted) {}
