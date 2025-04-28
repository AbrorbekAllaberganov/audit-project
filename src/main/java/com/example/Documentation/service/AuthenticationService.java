package com.example.Documentation.service;

import com.example.Documentation.dto.ApiResponse;
import com.example.Documentation.dto.AuthenticationRequest;
import com.example.Documentation.dto.RegisterRequest;
import com.example.Documentation.entity.Role;
import com.example.Documentation.entity.User;
import com.example.Documentation.jwtSecurity.JwtService;
import com.example.Documentation.repository.UserRepository;
import com.fasterxml.jackson.annotation.OptBoolean;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    public ApiResponse register(RegisterRequest registerRequest) {

        if (userRepository.existsByUserName(registerRequest.getUserName()))
            return new ApiResponse("userName already exists", false);

        User user = new User();
        user.setUserName(registerRequest.getUserName());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.USER);
        user.setActive(true);

        userRepository.save(user);
        String token = jwtService.generateToken(user);
        return new ApiResponse("JWT token",true,token);
    }

    public ApiResponse authenticate(AuthenticationRequest authenticationRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUserName(),
                        authenticationRequest.getPassword()
                )
        );

        Optional<User> userOptional = userRepository.findByUserName(authenticationRequest.getUserName());

        if (userOptional.isEmpty())
            return new ApiResponse("user not found", false);

        User user = userOptional.get();

        if (!user.isActive())
            return new ApiResponse("user is inactive", false);

        String token = jwtService.generateToken(user);
        return new ApiResponse("JWT token", true, token);
    }
}
