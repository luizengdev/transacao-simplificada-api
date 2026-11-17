package com.github.luizengdev.transacao_simplificada.controller;

import com.github.luizengdev.transacao_simplificada.domain.user.User;
import com.github.luizengdev.transacao_simplificada.domain.user.UserType;
import com.github.luizengdev.transacao_simplificada.domain.wallet.Wallet;
import com.github.luizengdev.transacao_simplificada.repository.TransactionRepository;
import com.github.luizengdev.transacao_simplificada.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.github.luizengdev.transacao_simplificada.services.AuthorizationService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

// Sobe a aplicação inteira em uma porta aleatória para o teste real
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockitoBean
    private AuthorizationService authorizationService;

    private Long payerId;
    private Long payeeId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        transactionRepository.deleteAll(); // 1º limpa os filhos (transações)
        userRepository.deleteAll(); // 2º limpa os pais (usuários e carteiras)

        // Mock padrão: autoriza as transferências a menos que especificado o contrário
        when(authorizationService.isAuthorized()).thenReturn(true);

        // Cria o pagador no banco real
        User payer = new User(null, "Pagador Teste", "11122233344", "pagador@teste.com", "123", UserType.COMMON);
        payer.setWallet(new Wallet(new BigDecimal("500.00"), payer));
        payerId = userRepository.save(payer).getId();

        // Cria o lojista recebedor no banco real
        User payee = new User(null, "Lojista Teste", "99988877700011", "lojista@teste.com", "123", UserType.MERCHANT);
        payee.setWallet(new Wallet(BigDecimal.ZERO, payee));
        payeeId = userRepository.save(payee).getId();
    }

    @Test
    @DisplayName("Deve realizar a transferência com sucesso entre dois usuários comuns")
    void shouldTransferSuccessfullyBetweenCommonUsers() {
        // Cria um segundo usuário comum para receber
        User receiver = new User(null, "Recebedor Teste", "22233344455", "recebedor@teste.com", "123", UserType.COMMON);
        receiver.setWallet(new Wallet(new BigDecimal("100.00"), receiver));
        Long receiverId = userRepository.save(receiver).getId();

        String payload = """
                {
                  "value": 150.0,
                  "payer": %d,
                  "payee": %d
                }
                """.formatted(payerId, receiverId);

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/transfer")
                .then()
                .statusCode(201);
    }

    @Test
    @DisplayName("Deve retornar erro quando o saldo for insuficiente")
    void shouldReturnErrorWhenBalanceIsInsufficient() {
        String payload = """
                {
                  "value": 600.0,
                  "payer": %d,
                  "payee": %d
                }
                """.formatted(payerId, payeeId);

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/transfer")
                .then()
                .statusCode(400)
                .body("message", equalTo("Saldo insuficiente para realizar a transferência."));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando um lojista tentar enviar dinheiro")
    void shouldReturnBadRequestWhenMerchantTriesToTransfer() {
        String payload = """
                {
                  "value": 50.0,
                  "payer": %d,
                  "payee": %d
                }
                """.formatted(payeeId, payerId);

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/transfer")
                .then()
                .statusCode(400)
                .body("message", equalTo("Lojistas não têm permissão para realizar transferências."));
    }
}
