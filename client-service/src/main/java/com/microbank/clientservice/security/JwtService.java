package com.microbank.clientservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtService {

  private final Key key;
  private final long expMinutes;

  public JwtService(@Value("${jwt.secret}") String secret,
                    @Value("${jwt.expMinutes}") long expMinutes) {
    this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64(secret)));
    this.expMinutes = expMinutes;
  }

  public String generateToken(UUID clientId, String email, Map<String, Object> claims) {
    Instant now = Instant.now();
    return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plusSeconds(expMinutes * 60)))
            .addClaims(claims)
            .claim("cid", clientId.toString())
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
  }

  public Key getKey() {
    return key;
  }

  private static String base64(String maybeRaw) {
    // If the provided secret isn't Base64, encode it to base64 so JJWT can use it.
    try {
      Decoders.BASE64.decode(maybeRaw);
      return maybeRaw; // already base64
    } catch (Exception e) {
      return java.util.Base64.getEncoder().encodeToString(maybeRaw.getBytes());
    }
  }
}
