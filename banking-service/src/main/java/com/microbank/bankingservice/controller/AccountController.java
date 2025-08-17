package com.microbank.bankingservice.controller;

import com.microbank.bankingservice.dto.AmountRequest;
import com.microbank.bankingservice.dto.BalanceResponse;
import com.microbank.bankingservice.dto.TxnResponse;
import com.microbank.bankingservice.model.Txn;
import com.microbank.bankingservice.security.JwtService;
import com.microbank.bankingservice.service.AccountService;
import com.microbank.bankingservice.service.ClientStatusClient;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

  private final AccountService accounts;
  private final ClientStatusClient clientStatus;
  private final JwtService jwtService;

  private Claims claimsFrom(String authz) {
    if (authz == null || !authz.startsWith("Bearer ")) throw new RuntimeException("Missing token");
    return jwtService.parse(authz.substring(7));
  }

  @PostMapping("/deposit")
  public BalanceResponse deposit(@RequestHeader("Authorization") String authz, @Valid @RequestBody AmountRequest req) {
    Claims claims = claimsFrom(authz);
    return accounts.deposit(claims, req.amount());
  }

  @PostMapping("/withdraw")
  public BalanceResponse withdraw(@RequestHeader("Authorization") String authz, @Valid @RequestBody AmountRequest req) {
    Claims claims = claimsFrom(authz);
    return accounts.withdraw(claims, req.amount());
  }

  @GetMapping("/balance")
  public BalanceResponse balance(@RequestHeader("Authorization") String authz) {
    return accounts.balance(claimsFrom(authz));
  }

  @GetMapping("/transactions")
  public List<TxnResponse> txns(@RequestHeader("Authorization") String authz) {
    return accounts.transactions(claimsFrom(authz)).stream()
        .map(t -> new TxnResponse(t.getId(), t.getType().name(), t.getAmount(), t.getBalanceAfter(), t.getCreatedAt()))
        .toList();
  }
}
