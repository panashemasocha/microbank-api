package com.microbank.clientservice.controller;

import com.microbank.clientservice.dto.ProfileResponse;
import com.microbank.clientservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProfileController {

  private final UserService users;

  @GetMapping("/me")
  public ProfileResponse me() {
    String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return users.me(email);
  }
}
