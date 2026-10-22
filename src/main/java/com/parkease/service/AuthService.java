package com.parkease.service;

import com.parkease.dto.*;
import com.parkease.entity.*;
import com.parkease.exception.ApiException;
import com.parkease.repository.UserRepository;
import com.parkease.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager auth;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, AuthenticationConfiguration config,
            JwtService jwt) throws Exception {
        this.users = users;
        this.encoder = encoder;
        this.auth = config.getAuthenticationManager();
        this.jwt = jwt;
    }

    public AuthResponse register(RegisterRequest req) {
        if (users.existsByEmail(req.email().toLowerCase()))
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        User u = new User();
        u.setName(req.name());
        u.setEmail(req.email().toLowerCase());
        u.setPassword(encoder.encode(req.password()));
        u.setRole(req.role() == Role.OWNER ? Role.OWNER : Role.USER);
        users.save(u);
        return response(u);
    }

    public AuthResponse login(AuthRequest req) {
        auth.authenticate(new UsernamePasswordAuthenticationToken(req.email().toLowerCase(), req.password()));
        return response(users.findByEmail(req.email().toLowerCase()).orElseThrow());
    }

    private AuthResponse response(User u) {
        UserDetails d = org.springframework.security.core.userdetails.User.withUsername(u.getEmail())
                .password(u.getPassword()).authorities("ROLE_" + u.getRole().name()).build();
        return new AuthResponse(jwt.generateToken(d), u.getId(), u.getName(), u.getEmail(), u.getRole());
    }
}
