package com.github.luizengdev.transacao_simplificada.services;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class AuthorizationService {

    private final RestClient restClient;

    public AuthorizationService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://util.devi.tools")
                .build();
    }

    public boolean isAuthorized() {
        try {
            ResponseEntity<Map> response = this.restClient.get()
                    .uri("/api/v2/authorize") // URL do Mock para simular o serviço
                    .retrieve()
                    .toEntity(Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                // Lê a estrutura padrão do mock {"status": "success", "data": {"authorization": true}}
                if ("success".equals(body.get("status"))) {
                    Map<String, Object> data = (Map<String, Object>) body.get("data");
                    if (data != null) {
                        return Boolean.TRUE.equals(data.get("authorization"));
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false; // Por segurança financeira, se o mock cair, nega a transação
        }
    }
}
