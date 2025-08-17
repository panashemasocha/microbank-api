package com.microbank.bankingservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TxnResponse(UUID id, String type, BigDecimal amount, BigDecimal balanceAfter, Instant createdAt) {}
