package com.microbank.bankingservice.service;

import com.microbank.bankingservice.dto.BalanceResponse;
import com.microbank.bankingservice.model.Account;
import com.microbank.bankingservice.model.Txn;
import com.microbank.bankingservice.repo.AccountRepository;
import com.microbank.bankingservice.repo.TxnRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

  private final AccountRepository accounts;
  private final TxnRepository txns;
  private final ClientStatusClient clientStatus;

  private UUID clientIdFromClaims(Claims claims) {
    return UUID.fromString((String) claims.get("cid"));
  }

  @Transactional
  public BalanceResponse deposit(Claims claims, BigDecimal amount) {
    UUID clientId = clientIdFromClaims(claims);
    Account acc = accounts.findByClientId(clientId).orElseGet(() -> accounts.save(Account.builder()
        .clientId(clientId).balance(BigDecimal.ZERO).build()));
    acc.setBalance(acc.getBalance().add(amount));
    accounts.save(acc);
    txns.save(Txn.builder()
        .clientId(clientId)
        .type(Txn.Type.DEPOSIT)
        .amount(amount)
        .balanceAfter(acc.getBalance())
        .build());
    return new BalanceResponse(acc.getBalance());
  }

  @Transactional
  public BalanceResponse withdraw(Claims claims, BigDecimal amount) {
    UUID clientId = clientIdFromClaims(claims);
    Account acc = accounts.findByClientId(clientId).orElseGet(() -> accounts.save(Account.builder()
        .clientId(clientId).balance(BigDecimal.ZERO).build()));
    if (acc.getBalance().compareTo(amount) < 0) {
      throw new RuntimeException("Insufficient funds");
    }
    acc.setBalance(acc.getBalance().subtract(amount));
    accounts.save(acc);
    txns.save(Txn.builder()
        .clientId(clientId)
        .type(Txn.Type.WITHDRAWAL)
        .amount(amount)
        .balanceAfter(acc.getBalance())
        .build());
    return new BalanceResponse(acc.getBalance());
  }

  @Transactional(readOnly = true)
  public BalanceResponse balance(Claims claims) {
    UUID clientId = clientIdFromClaims(claims);
    Account acc = accounts.findByClientId(clientId).orElseGet(() -> Account.builder()
        .clientId(clientId).balance(BigDecimal.ZERO).build());
    return new BalanceResponse(acc.getBalance());
  }

  @Transactional(readOnly = true)
  public List<Txn> transactions(Claims claims) {
    UUID clientId = clientIdFromClaims(claims);
    return txns.findByClientIdOrderByCreatedAtDesc(clientId);
  }
}
