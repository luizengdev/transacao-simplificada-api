package com.github.luizengdev.transacao_simplificada.controller;

import com.github.luizengdev.transacao_simplificada.domain.user.User;
import com.github.luizengdev.transacao_simplificada.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // Construtor puro (Sem Lombok)
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody User user) {
        User newUser = this.userService.saveUser(user);
        // Retorna o DTO de resposta protegendo a senha no cadastro também
        return new ResponseEntity<>(new UserResponseDTO(newUser), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<User> users = this.userService.getAllUsers();

        // Conversão usando Stream API do Java puro
        List<UserResponseDTO> responseDTOs = users.stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());

        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }
}
