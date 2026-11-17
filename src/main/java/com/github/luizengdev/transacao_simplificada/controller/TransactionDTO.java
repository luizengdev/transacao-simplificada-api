package com.github.luizengdev.transacao_simplificada.controller;

import java.math.BigDecimal;

public class TransactionDTO {
    private BigDecimal value;
    private Long payer;
    private Long payee;

    public TransactionDTO() {
    }

    public TransactionDTO(BigDecimal value, Long payer, Long payee) {
        this.value = value;
        this.payer = payer;
        this.payee = payee;
    }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public Long getPayer() { return payer; }
    public void setPayer(Long payer) { this.payer = payer; }

    public Long getPayee() { return payee; }
    public void setPayee(Long payee) { this.payee = payee; }
}

