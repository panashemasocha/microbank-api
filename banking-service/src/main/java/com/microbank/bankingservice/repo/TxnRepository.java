package com.microbank.bankingservice.repo;

import com.microbank.bankingservice.model.Txn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TxnRepository extends JpaRepository<Txn, UUID> {
  List<Txn> findByClientIdOrderByCreatedAtDesc(UUID clientId);
}
