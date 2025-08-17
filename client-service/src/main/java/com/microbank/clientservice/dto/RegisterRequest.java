package com.microbank.clientservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 3, max = 80) String fullName,
        @NotBlank @Size(min = 8, max = 128) String password
) {}
