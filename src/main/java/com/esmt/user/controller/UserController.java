package com.esmt.user.controller;

import com.esmt.user.dto.*;
import com.esmt.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Service", description = "Gestion des utilisateurs et Mobility Pass")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Inscription d'un nouvel utilisateur et création de pass")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un utilisateur par ID")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/passes/validate/{userId}")
    @Operation(summary = "Vérifier si le pass d'un utilisateur est actif et valide")
    public ResponseEntity<Boolean> validatePass(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.validatePass(userId));
    }

    @PutMapping("/pass/{passNumber}/suspend")
    @Operation(summary = "Suspendre un Mobility Pass")
    public ResponseEntity<PassResponse> suspendPass(@PathVariable String passNumber) {
        return ResponseEntity.ok(userService.suspendPass(passNumber));
    }

    @PutMapping("/pass/{passNumber}/reactivate")
    @Operation(summary = "Réactiver un Mobility Pass")
    public ResponseEntity<PassResponse> reactivatePass(@PathVariable String passNumber) {
        return ResponseEntity.ok(userService.reactivatePass(passNumber));
    }
}