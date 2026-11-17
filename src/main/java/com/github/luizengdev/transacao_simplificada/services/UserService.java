package com.github.luizengdev.transacao_simplificada.services;

import com.github.luizengdev.transacao_simplificada.domain.user.User;
import com.github.luizengdev.transacao_simplificada.domain.wallet.Wallet;
import com.github.luizengdev.transacao_simplificada.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

      @Transactional
    public User saveUser(User user) {
        // Valida se o CPF ou E-mail já existem antes de cadastrar
        if (userRepository.findByDocument(user.getDocument()).isPresent()) {
            throw new IllegalArgumentException("CPF/CNPJ já cadastrado no sistema.");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado no sistema.");
        }

        // Se o usuário já veio com um saldo preenchido no objeto (ex: vindo do JSON de cadastro)
        if (user.getWallet() != null && user.getWallet().getBalance() != null) {
            // Cria a carteira usando o saldo que foi enviado
            Wallet wallet = new Wallet(user.getWallet().getBalance(), user);
            user.setWallet(wallet);
        } else {
            // Caso contrário, se não enviou nada, começa com zero por segurança
            Wallet wallet = new Wallet(BigDecimal.ZERO, user);
            user.setWallet(wallet);
        }

        return this.userRepository.save(user);
    }

    // Retorna a lista completa para alimentar o seu GET do Controller
    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }
}
