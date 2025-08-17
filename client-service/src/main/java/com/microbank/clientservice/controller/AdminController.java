package com.microbank.clientservice.controller;

import com.microbank.clientservice.dto.UserResponse;
import com.microbank.clientservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.microbank.clientservice.model.User;
import com.microbank.clientservice.repo.UserRepository;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

  private final UserService users;
  private final UserRepository userRepository;

  @PostMapping("/blacklist/{id}")
  public String blacklist(@PathVariable("id") UUID id) {
    users.setBlacklist(id, true);
    return "Client blacklisted";
  }

  @PostMapping("/unblacklist/{id}")
  public String unblacklist(@PathVariable("id") UUID id) {
    users.setBlacklist(id, false);
    return "Client unblacklisted";
  }

  @GetMapping("/users")
  public List<UserResponse> listUsers() {
    return userRepository.findAll().stream()
            .map(u -> new UserResponse(u.getId(), u.getEmail(), u.isBlacklisted()))
            .toList();
  }


}
