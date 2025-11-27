package com.ticketguru.user_service.controller;

import com.ticketguru.user_service.dto.AuthRequest;
import com.ticketguru.user_service.dto.AuthResponse;
import com.ticketguru.user_service.dto.RegisterRequest;
import com.ticketguru.user_service.dto.UserDto;
import com.ticketguru.user_service.model.User;
import com.ticketguru.user_service.repository.UserRepository;
import com.ticketguru.user_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {

        String token = authService.generateToken(request.getUsername(), request.getPassword());


        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));


        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();


        long expireTime = System.currentTimeMillis() + (1000 * 60 * 30);


        AuthResponse response = AuthResponse.builder()
                .accessToken(token)
                .refreshToken(UUID.randomUUID().toString())
                .tokenType("Bearer")
                .accessTokenExpiresAt(expireTime)
                .user(userDto)
                .build();

        return ResponseEntity.ok(response);
    }
}