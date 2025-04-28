package com.example.Documentation.controller;

import com.example.Documentation.dto.ApiResponse;
import com.example.Documentation.dto.AuthenticationRequest;
import com.example.Documentation.dto.RegisterRequest;
import com.example.Documentation.dto.UserResponseDto;
import com.example.Documentation.jwtSecurity.JwtService;
import com.example.Documentation.service.AuthenticationService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public AuthController(AuthenticationService authenticationService, JwtService jwtService, UserDetailsService userDetailsService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest){
        ApiResponse response = authenticationService.register(registerRequest);
        return ResponseEntity.status(response.isSuccess()?200:409).body(response);
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest authenticationRequest){
        ApiResponse response = authenticationService.authenticate(authenticationRequest);
        return ResponseEntity.status(response.isSuccess()?200:404).body(response);
    }

    @GetMapping("/get-me")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String username = jwtService.extractUserName(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("UNKNOWN");

        return ResponseEntity.ok(new UserResponseDto(username, role));
    }


}
