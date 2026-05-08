package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.*;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.model.User;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.UserRepository;
import com.github.wsustudygroupapp.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

// Jose — handles all authentication logic
// register() → validate email domain, hash password, save user + profile, send verification email
// verify() → find user by token, mark as verified
// login() → check credentials, check verified, return JWT

@Slf4j
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

    @Value("${app.frontend-url}")
    private String frontendUrl; // http://localhost:5173

    @Value("${spring.mail.from:study.group.westfield@gmail.com}")
    private String mailFrom;

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
        log.info("register called for email={}", request.getEmail());

        // Only @westfield.ma.edu emails allowed — enforce the school domain
        if (!request.getEmail().endsWith(schoolEmailDomain))
        {
            log.warn("register rejected — invalid email domain for email={}", request.getEmail());
            throw new RuntimeException("Only @westfield.ma.edu emails are allowed");
        }

        // Prevent duplicate accounts — check if this email is already in the DB
        if (userRepository.existsByEmail(request.getEmail()))
        {
            log.warn("register rejected — duplicate email={}", request.getEmail());
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

        // Create a Profile linked to this User — save the display name they provided at registration
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setName(request.getName());
        profileRepository.save(profile);

        // Send the verification email — when they click it, GET /auth/verify?token=... fires
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(request.getEmail());
        message.setSubject("Verify your WSU Study Group account");
        message.setText("Click to verify your account: " + frontendUrl + "/verify?token=" + verificationToken);
        mailSender.send(message);
    }

    /*
        Called when the student clicks the verification link in their email.
        We look up the user by that UUID token, mark them verified, and clear the token.
     */
    public void verify(String token)
    {
        log.info("verify called for token={}", token);

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
        log.info("login called for email={}", request.getEmail());

        // Look up the user by email — throw if no account exists
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Compare the raw password against the BCrypt hash stored in the DB
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
        {
            log.warn("login rejected — invalid password for email={}", request.getEmail());
            throw new RuntimeException("Invalid password");
        }

        // Block login if they skipped email verification
        if (!user.isVerified())
        {
            log.warn("login rejected — unverified account for email={}", request.getEmail());
            throw new RuntimeException("Please verify your email before logging in");
        }

        // All checks passed — generate the JWT and hand it back to the client
        // From this point on the client sends: Authorization: Bearer <token> on every request
        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, "Login successful");
    }

    public void resendVerification(String email) {
        log.info("resendVerification called for email={}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with that email"));

        if (user.isVerified()) {
            log.warn("resendVerification rejected — account already verified for email={}", email);
            throw new RuntimeException("This account is already verified. You can log in.");
        }

        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        userRepository.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject("Verify your WSU Study Group account");
        message.setText("Click to verify your account: " + frontendUrl + "/verify?token=" + verificationToken);
        mailSender.send(message);
    }

    public void forgotPassword(ForgotPasswordRequest request)
    {
        log.info("forgotPassword called for email={}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new RuntimeException("No account found with that email"));

        // Generate a single-use reset token and save it to the user
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        userRepository.save(user);

        // Email the reset link — frontend /reset-password?token=... page handles it
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(request.getEmail());
        message.setSubject("Reset your WSU Study Group password");
        message.setText("Click to reset your password: " + frontendUrl + "/reset-password?token=" + resetToken);
        mailSender.send(message);
    }

    /*
        Update password for a logged-in user who knows their current password.
        Gets the user from the JWT (SecurityContextHolder) — no token needed in the request body.
        Verifies current password before allowing the change.
     */
    public void updatePassword(UpdatePasswordRequest request)
    {
        // Pull the authenticated user's email from the JWT — Spring Security sets this after JwtAuthFilter runs
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("updatePassword called for email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Make sure they actually know their current password before letting them change it
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword()))
        {
            log.warn("updatePassword rejected — incorrect current password for email={}", email);
            throw new RuntimeException("Current password is incorrect");
        }

        if (request.getNewPassword().length() < 8)
        {
            log.warn("updatePassword rejected — new password too short for email={}", email);
            throw new RuntimeException("New password must be at least 8 characters");
        }

        // Hash and save — never store plain text
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public void changePassword(ChangePasswordRequest request)
    {
        log.info("changePassword called via reset token");
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (request.getNewPassword().length() < 8)
        {
            throw new RuntimeException("Password must be at least 8 characters");
        }

        // Hash the new password — never store plain text
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        // Clear the token so the reset link can't be reused
        user.setResetToken(null);
        userRepository.save(user);
    }
}
