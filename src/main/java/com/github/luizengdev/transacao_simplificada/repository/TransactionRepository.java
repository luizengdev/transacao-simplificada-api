package com.github.luizengdev.transacao_simplificada.repository;

import com.github.luizengdev.transacao_simplificada.domain.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
