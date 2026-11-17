package com.github.luizengdev.transacao_simplificada.repository;

import com.github.luizengdev.transacao_simplificada.domain.wallet.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // Bloqueia a linha da carteira no banco para atualizações seguras de saldo
    // Evita que dois processos tentem alterar o saldo da mesma carteira ao mesmo tempo
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdForUpdate(Long id);
}
