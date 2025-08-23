package com.enterprise.gestaoestoque.controller;

import com.enterprise.gestaoestoque.configuration.jwt.JWTUtil;
import com.enterprise.gestaoestoque.model.dto.authentication.LoginRequestDTO;
import com.enterprise.gestaoestoque.model.dto.authentication.LoginResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(), loginRequest.password()
                )
        );

        var authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority).toList();

        var token = jwtUtil.generateToken(authentication.getName(), authorities);

        return ResponseEntity.ok(new LoginResponseDTO("Login successful", token));
    }
}
