package com.github.luizengdev.transacao_simplificada.services;

import com.github.luizengdev.transacao_simplificada.domain.transaction.Transaction;
import com.github.luizengdev.transacao_simplificada.domain.user.User;
import com.github.luizengdev.transacao_simplificada.domain.user.UserType;
import com.github.luizengdev.transacao_simplificada.domain.wallet.Wallet;
import com.github.luizengdev.transacao_simplificada.repository.TransactionRepository;
import com.github.luizengdev.transacao_simplificada.repository.UserRepository;
import com.github.luizengdev.transacao_simplificada.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TransactionService transactionService;

    private User commonUser;
    private User merchantUser;
    private Wallet commonWallet;
    private Wallet merchantWallet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Instancia os usuários usando o construtor completo com ID para os cenários de simulação
        commonUser = new User(1L, "Usuário Comum", "12345678901", "comum@email.com", "senha123", UserType.COMMON);
        merchantUser = new User(2L, "Lojista", "98765432100012", "lojista@email.com", "senha123", UserType.MERCHANT);

        // Inicializa as carteiras com saldos específicos
        commonWallet = new Wallet(new BigDecimal("500.00"), commonUser);
        commonWallet.setId(10L);
        commonUser.setWallet(commonWallet);

        merchantWallet = new Wallet(new BigDecimal("200.00"), merchantUser);
        merchantWallet.setId(20L);
        merchantUser.setWallet(merchantWallet);
    }

    @Test
    @DisplayName("Deve realizar a transferência com sucesso quando todos os dados e validações forem válidos")
    void transferSuccess() {
        // Arrange (Configuração dos comportamentos simulados dos mocks)
        BigDecimal transferValue = new BigDecimal("100.00");

        when(userRepository.findById(1L)).thenReturn(Optional.of(commonUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(merchantUser));

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(commonWallet));
        when(walletRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(merchantWallet));

        when(authorizationService.isAuthorized()).thenReturn(true);

        Transaction fakeTransaction = new Transaction(transferValue, commonUser, merchantUser);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(fakeTransaction);

        // Act (Execução da regra de negócio que está sendo testada)
        Transaction result = transactionService.transfer(1L, 2L, transferValue);

        // Assert (Verificação se o estado final condiz com as regras de negócio)
        assertNotNull(result);
        assertEquals(new BigDecimal("400.00"), commonWallet.getBalance()); // 500 - 100
        assertEquals(new BigDecimal("300.00"), merchantWallet.getBalance()); // 200 + 100

        // Verifica se os métodos cruciais foram de fato acionados no fluxo
        verify(walletRepository, times(1)).save(commonWallet);
        verify(walletRepository, times(1)).save(merchantWallet);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(notificationService, times(1)).sendNotification(eq("lojista@email.com"), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção e não alterar saldos quando o pagador for um Lojista")
    void transferErrorPayerIsMerchant() {
        // Arrange
        BigDecimal transferValue = new BigDecimal("50.00");

        // Cenário invertido: lojista tentando pagar para o usuário comum
        when(userRepository.findById(2L)).thenReturn(Optional.of(merchantUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(commonUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.transfer(2L, 1L, transferValue);
        });

        assertEquals("Lojistas não têm permissão para realizar transferências.", exception.getMessage());

        // Garante que o dinheiro não mudou de lugar e nenhuma persistência foi chamada
        assertEquals(new BigDecimal("500.00"), commonWallet.getBalance());
        assertEquals(new BigDecimal("200.00"), merchantWallet.getBalance());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o pagador não tiver saldo suficiente")
    void transferErrorInsufficientBalance() {
        // Arrange
        BigDecimal transferValue = new BigDecimal("600.00"); // Maior do que os 500.00 disponíveis

        when(userRepository.findById(1L)).thenReturn(Optional.of(commonUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(merchantUser));
        when(walletRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(commonWallet));
        when(walletRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(merchantWallet));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.transfer(1L, 2L, transferValue);
        });

        assertEquals("Saldo insuficiente para realizar a transferência.", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção e reverter quando o serviço autorizador externo recusar a transação")
    void transferErrorNotAuthorized() {
        // Arrange
        BigDecimal transferValue = new BigDecimal("100.00");

        when(userRepository.findById(1L)).thenReturn(Optional.of(commonUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(merchantUser));
        when(walletRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(commonWallet));
        when(walletRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(merchantWallet));

        // Simula o serviço de autorização retornando falso
        when(authorizationService.isAuthorized()).thenReturn(false);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            transactionService.transfer(1L, 2L, transferValue);
        });

        assertEquals("Transferência não autorizada pelo serviço externo.", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }
}
