package com.github.luizengdev.transacao_simplificada.controller;

import com.github.luizengdev.transacao_simplificada.domain.transaction.Transaction;
import com.github.luizengdev.transacao_simplificada.services.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transfer")
public class TransactionController {

    private final TransactionService transactionService;

    // Injeção de dependência via construtor puro
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransfer(@RequestBody TransactionDTO dto) {
        Transaction newTransaction = this.transactionService.transfer(
                dto.getPayer(),
                dto.getPayee(),
                dto.getValue()
        );
        return new ResponseEntity<>(newTransaction, HttpStatus.CREATED);
    }
}
