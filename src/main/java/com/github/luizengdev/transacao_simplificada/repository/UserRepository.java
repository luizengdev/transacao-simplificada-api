package com.github.luizengdev.transacao_simplificada.repository;

import com.github.luizengdev.transacao_simplificada.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Permite buscar por CPF/CNPJ para validar se já existe cadastro
    Optional<User> findByDocument(String document);

    // Permite buscar por E-mail para validar se já existe cadastro
    Optional<User> findByEmail(String email);
}
