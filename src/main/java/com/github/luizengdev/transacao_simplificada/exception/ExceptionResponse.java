package com.github.luizengdev.transacao_simplificada.exception;

import java.time.LocalDateTime;

public class ExceptionResponse {
    private String message;
    private int status;
    private LocalDateTime timestamp;

    public ExceptionResponse() {
    }

    public ExceptionResponse(String message, int status, LocalDateTime timestamp) {
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters e Setters puros
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
