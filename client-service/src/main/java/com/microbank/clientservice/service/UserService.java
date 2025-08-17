package com.microbank.clientservice.service;

import com.microbank.clientservice.dto.AuthResponse;
import com.microbank.clientservice.dto.LoginRequest;
import com.microbank.clientservice.dto.ProfileResponse;
import com.microbank.clientservice.dto.RegisterRequest;
import com.microbank.clientservice.model.User;
import com.microbank.clientservice.repo.UserRepository;
import com.microbank.clientservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements CommandLineRunner {

  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final JwtService jwt;

  @Value("${admin.email}")
  private String adminEmail;
  @Value("${admin.password}")
  private String adminPassword;

  @Transactional
  public AuthResponse register(RegisterRequest req) {
    users.findByEmail(req.email()).ifPresent(u -> { throw new RuntimeException("Email already registered"); });
    User u = User.builder()
        .email(req.email().toLowerCase())
        .fullName(req.fullName())
        .passwordHash(encoder.encode(req.password()))
        .roles(Set.of("USER"))
        .blacklisted(false)
        .build();
    users.save(u);
    String token = jwt.generateToken(u.getId(), u.getEmail(), Map.of(
        "roles", u.getRoles(),
        "bl", u.isBlacklisted()
    ));
    return new AuthResponse(token, u.getId(), u.getEmail(), u.getFullName(), u.getRoles(), u.isBlacklisted());
  }

  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest req) {
    User u = users.findByEmail(req.email().toLowerCase()).orElseThrow(() -> new RuntimeException("Invalid credentials"));
    if (!encoder.matches(req.password(), u.getPasswordHash())) {
      throw new RuntimeException("Invalid credentials");
    }
    String token = jwt.generateToken(u.getId(), u.getEmail(), Map.of("roles", u.getRoles(), "bl", u.isBlacklisted()));
    return new AuthResponse(token, u.getId(), u.getEmail(), u.getFullName(), u.getRoles(), u.isBlacklisted());
  }

  @Transactional(readOnly = true)
  public ProfileResponse me(String email) {
    User u = users.findByEmail(email.toLowerCase()).orElseThrow();
    return new ProfileResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRoles(), u.isBlacklisted());
  }

  @Transactional
  public void setBlacklist(UUID id, boolean value) {
    User u = users.findById(id).orElseThrow();
    u.setBlacklisted(value);
    users.save(u);
  }

  @Override
  public void run(String... args) {
    // Seed admin user if not exists
    users.findByEmail(adminEmail.toLowerCase()).or(() -> {
      User admin = User.builder()
          .email(adminEmail.toLowerCase())
          .fullName("System Admin")
          .passwordHash(encoder.encode(adminPassword))
          .roles(Set.of("ADMIN", "USER"))
          .blacklisted(false)
          .build();
      users.save(admin);
      return java.util.Optional.of(admin);
    });
  }
}
