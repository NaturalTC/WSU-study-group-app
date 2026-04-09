package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.AuthResponse;
import com.github.wsustudygroupapp.dto.LoginRequest;
import com.github.wsustudygroupapp.dto.RegisterRequest;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.model.User;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.UserRepository;
import com.github.wsustudygroupapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

// Jose — handles all authentication logic
// register() → validate email domain, hash password, save user + profile, send verification email
// verify() → find user by token, mark as verified
// login() → check credentials, check verified, return JWT

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil;

    // Pull school email domain and base URL from application-local.properties
    @Value("${app.school-email-domain}")
    private String schoolEmailDomain; // @westfield.ma.edu

    @Value("${app.base-url}")
    private String baseUrl; // http://localhost:8080

    // Spring injects all dependencies through this constructor at startup
    public AuthService(UserRepository userRepository,
                       ProfileRepository profileRepository,
                       PasswordEncoder passwordEncoder,
                       JavaMailSender mailSender,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.jwtUtil = jwtUtil;
    }

    /*
        Register a new student account.
        We validate the school email, hash their password, save the user,
        create a blank profile, and fire off a verification email.
        They can't log in until they click the link we send them.
     */
    public void register(RegisterRequest request)
    {

        // Only @westfield.ma.edu emails allowed — enforce the school domain
        if (!request.getEmail().endsWith(schoolEmailDomain))
        {
            throw new RuntimeException("Only @westfield.ma.edu emails are allowed");
        }

        // Prevent duplicate accounts — check if this email is already in the DB
        if (userRepository.existsByEmail(request.getEmail()))
        {
            throw new RuntimeException("Email already registered");
        }

        // BCrypt hash the password — we never store plain text passwords
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Generate a random UUID token to be used as the email verification link
        // Single use — gets cleared from DB once they verify
        String verificationToken = UUID.randomUUID().toString();

        // Build the User object and save it
        // isVerified = false until they click the email link
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(hashedPassword);
        user.setRole("USER");
        user.setVerified(false);
        user.setVerificationToken(verificationToken);
        userRepository.save(user);

        // Create a blank Profile linked to this User
        // Profile holds student data (name, major, bio) — kept separate from auth credentials
        Profile profile = new Profile();
        profile.setUser(user);
        profileRepository.save(profile);

        // Send the verification email — when they click it, GET /auth/verify?token=... fires
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(request.getEmail());
        message.setSubject("Verify your WSU Study Group account");
        message.setText("Click to verify your account: " + baseUrl + "/auth/verify?token=" + verificationToken);
        mailSender.send(message);
    }

    /*
        Called when the student clicks the verification link in their email.
        We look up the user by that UUID token, mark them verified, and clear the token.
     */
    public void verify(String token)
    {

        // Look up the user by their verification token — throw if the token doesn't exist
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        // Mark verified and wipe the token — it's single use, can't be clicked again
        user.setVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    /*
        Login — validate credentials and return a JWT token.
        The client stores this token and sends it in every future request header.
        This is the moment our app goes stateless — no sessions, just the token.
     */
    public AuthResponse login(LoginRequest request)
    {

        // Look up the user by email — throw if no account exists
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Compare the raw password against the BCrypt hash stored in the DB
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
        {
            throw new RuntimeException("Invalid password");
        }

        // Block login if they skipped email verification
        if (!user.isVerified())
        {
            throw new RuntimeException("Please verify your email before logging in");
        }

        // All checks passed — generate the JWT and hand it back to the client
        // From this point on the client sends: Authorization: Bearer <token> on every request
        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, "Login successful");
    }
}
