package com.enterprise.gestaoestoque.model.dto.authentication;

public record LoginResponseDTO(
        String status,
        String jwtToken
) {
}
