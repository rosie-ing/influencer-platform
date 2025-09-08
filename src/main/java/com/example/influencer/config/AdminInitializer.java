package com.example.influencer.config;

import com.example.influencer.domain.Role;
import com.example.influencer.domain.User;
import com.example.influencer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
@Profile("local") // 로컬에서만 동작
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String email = "admin@example.com";
        if (userRepository.findByEmail(email).isEmpty()) {
            User admin = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode("admin1234!"))
                    .nickname("관리자")
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            System.out.println("[INIT] Admin user created: " + email + " / admin1234!");
        } else {
            System.out.println("[INIT] Admin user already exists: " + email);
        }
    }
}
