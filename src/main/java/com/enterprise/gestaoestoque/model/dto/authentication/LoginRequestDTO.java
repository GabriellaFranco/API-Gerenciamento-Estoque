package com.enterprise.gestaoestoque.model.dto.authentication;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(

        @NotBlank
        String username,

        @NotBlank
        String password
) {
}
