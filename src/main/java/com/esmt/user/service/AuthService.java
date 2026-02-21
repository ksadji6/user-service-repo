package com.esmt.user.service;

import com.esmt.user.dto.AuthResponse;
import com.esmt.user.dto.LoginRequest;
import com.esmt.user.entity.User;
import com.esmt.user.repository.UserRepository;
import com.esmt.user.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthResponse login(LoginRequest request) {
        // 1. Rechercher l'utilisateur par email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Identifiants invalides"));

        // 2. Vérifier le mot de passe avec BCrypt
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Échec de connexion pour l'utilisateur : {}", request.getEmail());
            throw new RuntimeException("Identifiants invalides");
        }

        // 3. Générer le token JWT
        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name(), user.getId());

        log.info("Utilisateur connecté avec succès : {}", user.getEmail());

        // 4. Retourner la réponse attendue
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}