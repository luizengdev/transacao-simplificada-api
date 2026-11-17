package com.github.luizengdev.transacao_simplificada.controller;

import com.github.luizengdev.transacao_simplificada.domain.user.User;
import com.github.luizengdev.transacao_simplificada.domain.user.UserType;
import java.math.BigDecimal;

public class UserResponseDTO {
    private Long id;
    private String fullName;
    private String document;
    private String email;
    private UserType userType;
    private BigDecimal balance; // Exposto de forma direta e limpa aqui

    public UserResponseDTO() {
    }

    // Construtor auxiliar para converter a Entity pura para o DTO de Saída
    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.document = user.getDocument();
        this.email = user.getEmail();
        this.userType = user.getUserType();
        if (user.getWallet() != null) {
            this.balance = user.getWallet().getBalance();
        } else {
            this.balance = BigDecimal.ZERO;
        }
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDocument() { return document; }
    public void setDocument(String document) { this.document = document; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
