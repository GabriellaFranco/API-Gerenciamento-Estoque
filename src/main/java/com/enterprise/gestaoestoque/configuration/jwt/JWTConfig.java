package com.enterprise.gestaoestoque.configuration.jwt;

import com.enterprise.gestaoestoque.constants.ApplicationConstants;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

import static ch.qos.logback.core.encoder.ByteArrayUtil.hexStringToByteArray;

@Configuration
public class JWTConfig {

    @Value("${" + ApplicationConstants.JWT_SECRET_KEY + ":" + ApplicationConstants.JWT_SECRET_DEFAULT_VALUE + "}")
    private String secret;

    @Bean
    public SecretKey jwtSecretKey() {
        return Keys.hmacShaKeyFor(hexStringToByteArray(secret));
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    @Bean
    public JwtParser jwtParser(SecretKey jwtSecretKey) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build();
    }
}
