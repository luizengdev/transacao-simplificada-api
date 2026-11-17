package com.github.luizengdev.transacao_simplificada.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    private final RestClient restClient;

    public NotificationService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://util.devi.tools")
                .build();
    }

    public void sendNotification(String email, String message) {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("email", email);
            payload.put("message", message);

            this.restClient.post()
                    .uri("/api/v1/notify") // Mock para simular o envio da notificação
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            System.out.println("Notificação enviada com sucesso para: " + email);
        } catch (Exception e) {
            // Tratamento exigido: Apenas loga. A instabilidade de terceiros não cancela a transferência
            System.err.println("Serviço de notificação instável para " + email + ": " + e.getMessage());
        }
    }
}
