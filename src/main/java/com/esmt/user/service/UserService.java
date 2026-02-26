package com.esmt.user.service;

import com.esmt.user.dto.*;
import com.esmt.user.entity.*;
import com.esmt.user.event.UserRegisteredEvent;
import com.esmt.user.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final MobilityPassRepository passRepository;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.rabbitmq.exchange:smart-mobility.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.user-registered:user.registered.event}")
    private String userRegisteredKey;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Validation unicité email (Cas TC-US-02)
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("Tentative d'inscription avec un email existant : {}", request.getEmail());
            throw new RuntimeException("Email already in use");
        }

        // 1. Création de l'utilisateur
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);

        // 2. Génération du Mobility Pass
        String passNumber = "MP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        MobilityPass pass = MobilityPass.builder()
                .passNumber(passNumber)
                .user(user)
                .passType(PassType.STANDARD)
                .status(PassStatus.ACTIVE)
                .build();

        passRepository.save(pass);
        user.setMobilityPass(pass);

        // 3. Notification asynchrone via RabbitMQ
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .passNumber(passNumber)
                .build();

        /*try {
            // Conversion manuelle de l'objet 'event' en chaîne JSON
            String jsonEvent = objectMapper.writeValueAsString(event);

            // Envoi de la chaîne JSON au lieu de l'objet brut
            rabbitTemplate.convertAndSend(exchange, userRegisteredKey, jsonEvent);

            log.info("Inscription réussie et événement envoyé pour : {}", user.getEmail());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            // Si la conversion échoue, on log l'erreur sans bloquer l'inscription en base
            log.error("Erreur conversion JSON Inscription : {}", e.getMessage());
        }
*/
        return toUserResponse(user);
    }

    /**
     * Utilisé par TripService pour valider l'éligibilité au trajet
     */
    @Transactional(readOnly = true)
    public boolean validatePass(Long userId) {
        return passRepository.findByUserId(userId)
                .map(MobilityPass::isValid)
                .orElse(false);
    }

    @Transactional
    public PassResponse suspendPass(String passNumber) {
        MobilityPass pass = passRepository.findByPassNumber(passNumber)
                .orElseThrow(() -> new RuntimeException("Pass non trouvé: " + passNumber));
        pass.setStatus(PassStatus.SUSPENDED);
        log.warn("Pass suspendu : {}", passNumber);
        return toPassResponse(passRepository.save(pass));
    }

    @Transactional
    public PassResponse reactivatePass(String passNumber) {
        MobilityPass pass = passRepository.findByPassNumber(passNumber)
                .orElseThrow(() -> new RuntimeException("Pass non trouvé: " + passNumber));
        pass.setStatus(PassStatus.ACTIVE);
        log.info("Pass réactivé : {}", passNumber);
        return toPassResponse(passRepository.save(pass));
    }

    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::toUserResponse)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé ID: " + id));
    }

    // Mappers privés (DTOs)
    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .uuid(user.getUuid())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .totalTrips(user.getTotalTrips())
                .mobilityPass(user.getMobilityPass() != null ? toPassResponse(user.getMobilityPass()) : null)
                .build();
    }

    private PassResponse toPassResponse(MobilityPass pass) {
        return PassResponse.builder()
                .id(pass.getId())
                .passNumber(pass.getPassNumber())
                .passType(pass.getPassType().name())
                .status(pass.getStatus().name())
                .issuedAt(pass.getIssuedAt())
                .expiresAt(pass.getExpiresAt())
                .valid(pass.isValid())
                .build();
    }
    @Transactional
    public void incrementTripCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setTotalTrips(user.getTotalTrips() + 1);
        userRepository.save(user);
        log.info("Compteur de trajets incrémenté pour l'utilisateur {}", userId);
    }

}