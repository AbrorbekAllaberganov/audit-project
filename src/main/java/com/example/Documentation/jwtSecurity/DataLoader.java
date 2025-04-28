package com.example.Documentation.jwtSecurity;


import com.example.Documentation.entity.Role;
import com.example.Documentation.entity.User;
import com.example.Documentation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String init;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (init.equals("create")){
            User user = new User();
            user.setRole(Role.ADMIN);
            user.setUserName("string");
            user.setPassword(passwordEncoder.encode("123"));
            user.setFullName("Ortuxa");
            user.setActive(true);

            userRepository.save(user);

        }
    }
}
