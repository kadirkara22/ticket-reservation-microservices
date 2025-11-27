package com.ticketguru.user_service.service;

import com.ticketguru.user_service.dto.RegisterRequest;
import com.ticketguru.user_service.model.User;
import com.ticketguru.user_service.repository.UserRepository;
import com.ticketguru.user_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String register(RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Bu kullanıcı adı zaten alınmış!");
        }


        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu email adresi ile zaten kayıt olunmuş!");
        }


        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .build();


        userRepository.save(user);

        return "Kullanıcı başarıyla kaydedildi!";
    }


    public String generateToken(String username, String password) {

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isPresent()) {

            if (passwordEncoder.matches(password, user.get().getPassword())) {

                return jwtUtil.generateToken(username);
            } else {
                throw new RuntimeException("Hatalı Şifre!");
            }
        } else {
            throw new RuntimeException("Kullanıcı Bulunamadı!");
        }
    }
}