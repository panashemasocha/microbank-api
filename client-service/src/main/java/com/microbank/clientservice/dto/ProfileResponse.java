package com.microbank.clientservice.dto;

import java.util.Set;
import java.util.UUID;

public record ProfileResponse(UUID id, String email, String fullName, Set<String> roles, boolean blacklisted) {}
