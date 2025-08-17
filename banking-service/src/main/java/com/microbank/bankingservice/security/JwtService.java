package com.microbank.bankingservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Map;

@Component
public class JwtService {

  private final Key key;

  public JwtService(@Value("${jwt.secret}") String secret) {
    this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64(secret)));
  }

  public Claims parse(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private static String base64(String maybeRaw) {
    try {
      Decoders.BASE64.decode(maybeRaw);
      return maybeRaw;
    } catch (Exception e) {
      return java.util.Base64.getEncoder().encodeToString(maybeRaw.getBytes());
    }
  }
}
