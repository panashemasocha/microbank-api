package com.microbank.clientservice.dto;

import java.util.UUID;

public record UserResponse(UUID id, String email, boolean blacklisted) {}
