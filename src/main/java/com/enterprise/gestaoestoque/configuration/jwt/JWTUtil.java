package com.enterprise.gestaoestoque.configuration.jwt;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Component
public class JWTUtil {

    private final SecretKey secretKey;

    public String generateToken(String user, List<String> authorities) {
        List<String> rolesWithPrefix = authorities.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .toList();

        return Jwts.builder()
                .setIssuer("stock-management")
                .setSubject(user)
                .claim("authorities", rolesWithPrefix)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000000 * 60 * 60))
                .signWith(secretKey)
                .compact();
    }
}
