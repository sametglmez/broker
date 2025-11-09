package com.example.broker.controller;

import com.example.broker.model.LoginResponse;
import com.example.broker.model.RegisterRequest;
import com.example.broker.model.RegisterResponseModel;
import com.example.broker.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseModel> register(
            @RequestBody RegisterRequest registerRequest ) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestParam String username,
            @RequestParam String password
    ) {
        return ResponseEntity.ok(authService.login(username, password));
    }
}
