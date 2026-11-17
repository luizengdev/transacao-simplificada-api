package com.github.luizengdev.transacao_simplificada.services;

import com.github.luizengdev.transacao_simplificada.domain.transaction.Transaction;
import com.github.luizengdev.transacao_simplificada.domain.user.User;
import com.github.luizengdev.transacao_simplificada.domain.wallet.Wallet;
import com.github.luizengdev.transacao_simplificada.repository.TransactionRepository;
import com.github.luizengdev.transacao_simplificada.repository.UserRepository;
import com.github.luizengdev.transacao_simplificada.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class TransactionService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final AuthorizationService authorizationService;
    private final NotificationService notificationService;

    // Injeção de dependências via Construtor puro (Sem Lombok)
    public TransactionService(UserRepository userRepository,
                              WalletRepository walletRepository,
                              TransactionRepository transactionRepository,
                              AuthorizationService authorizationService,
                              NotificationService notificationService) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.authorizationService = authorizationService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Transaction transfer(Long payerId, Long payeeId, BigDecimal value) {
        // 1. Busca os usuários envolvidos
        User payer = this.userRepository.findById(payerId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário pagador não encontrado."));

        User payee = this.userRepository.findById(payeeId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário recebedor não encontrado."));

        // 2. Validação de Negócio: Lojistas não podem enviar dinheiro
        if (!payer.canSendMoney()) {
            throw new IllegalArgumentException("Lojistas não têm permissão para realizar transferências.");
        }

        // 3. Busca as carteiras aplicando LOCK pessimista (Garante consistência atômica no banco)
        Wallet payerWallet = this.walletRepository.findByIdForUpdate(payer.getWallet().getId())
                .orElseThrow(() -> new IllegalArgumentException("Carteira do pagador não encontrada."));

        Wallet payeeWallet = this.walletRepository.findByIdForUpdate(payee.getWallet().getId())
                .orElseThrow(() -> new IllegalArgumentException("Carteira do recebedor não encontrada."));

        // 4. Executa o débito (aqui dentro já valida se tem saldo suficiente)
        payerWallet.debit(value);
        payeeWallet.credit(value);

        // 5. Consulta o Serviço Autorizador Externo
        boolean authorized = this.authorizationService.isAuthorized();
        if (!authorized) {
            throw new IllegalStateException("Transferência não autorizada pelo serviço externo.");
        }

        // 6. Registra e persiste o histórico da transação imutável
        Transaction transaction = new Transaction(value, payer, payee);
        Transaction savedTransaction = this.transactionRepository.save(transaction);

        // 7. Salva os novos estados das carteiras atualizadas
        this.walletRepository.save(payerWallet);
        this.walletRepository.save(payeeWallet);

        // 8. Dispara as notificações de forma assíncrona/segura pós-transferência
        this.notificationService.sendNotification(payee.getEmail(), "Você recebeu um pagamento no valor de R$ " + value);

        return savedTransaction;

    }
}
