package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.AuthResponse;
import com.github.wsustudygroupapp.dto.LoginRequest;
import com.github.wsustudygroupapp.dto.RegisterRequest;
import com.github.wsustudygroupapp.repository.UserRepository;
import com.github.wsustudygroupapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// TODO: Jose — handles all authentication logic
// register() → validate email domain, hash password, save user + profile, send verification email
// verify() → find user by token, mark as verified
// login() → check credentials, check verified, return JWT

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil;

    @Value("${app.school-email-domain}")
    private String schoolEmailDomain; // @westfield.ma.edu

    @Value("${app.base-url}")
    private String baseUrl; // http://localhost:8080

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JavaMailSender mailSender,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.jwtUtil = jwtUtil;
    }

    // TODO: Step 1 — validate the email ends with @westfield.ma.edu
    //  if (!request.getEmail().endsWith(schoolEmailDomain)) throw exception

    // TODO: Step 2 — check the email isn't already registered
    //  if (userRepository.existsByEmail(email)) throw exception

    // TODO: Step 3 — hash the password
    //  String hashed = passwordEncoder.encode(request.getPassword())

    // TODO: Step 4 — generate a UUID verification token
    //  String token = UUID.randomUUID().toString()

    // TODO: Step 5 — save the new User (isVerified = false, role = "USER")

    // TODO: Step 6 — create and save a blank Profile linked to the User

    // TODO: Step 7 — send the verification email
    //  link = baseUrl + "/auth/verify?token=" + token
    public void register(RegisterRequest request) {

    }

    // TODO: find user by verificationToken
    // TODO: if not found, throw exception
    // TODO: set isVerified = true, clear the token, save
    public void verify(String token) {

    }

    // TODO: find user by email — throw exception if not found
    // TODO: check passwordEncoder.matches(rawPassword, hashedPassword)
    // TODO: check user.isVerified() — throw exception if not verified yet
    // TODO: generate and return a JWT token using jwtUtil.generateToken(email)
    public AuthResponse login(LoginRequest request) {
        return null;
    }
}
