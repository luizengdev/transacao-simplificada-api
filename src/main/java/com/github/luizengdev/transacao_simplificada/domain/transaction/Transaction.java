package com.github.luizengdev.transacao_simplificada.domain.transaction;

import com.github.luizengdev.transacao_simplificada.domain.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id")
    private User payer; // Usuário que envia (Origem)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payee_id")
    private User payee; // Usuário que recebe (Destino)

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    private void PrePersist() {
        timestamp = LocalDateTime.now();
    }

    public Transaction() {
    }

    public Transaction(BigDecimal value, User payer, User payee) {
        this.value = value;
        this.payer = payer;
        this.payee = payee;
    }

     public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public User getPayer() {
        return payer;
    }

    public void setPayer(User payer) {
        this.payer = payer;
    }

    public User getPayee() {
        return payee;
    }

    public void setPayee(User payee) {
        this.payee = payee;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
