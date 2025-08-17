package com.microbank.clientservice.controller;

import com.microbank.clientservice.dto.AuthResponse;
import com.microbank.clientservice.dto.LoginRequest;
import com.microbank.clientservice.dto.RegisterRequest;
import com.microbank.clientservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserService users;

  @PostMapping("/register")
  public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
    return users.register(req);
  }

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest req) {
    return users.login(req);
  }
}
