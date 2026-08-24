package com.example.resumeanalyzerbackend.controller;

import com.example.resumeanalyzerbackend.dto.AuthRequest;
import com.example.resumeanalyzerbackend.dto.AuthResponse;
import com.example.resumeanalyzerbackend.dto.RegisterRequest;
import com.example.resumeanalyzerbackend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        System.out.println("REGISTER API HIT");
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
