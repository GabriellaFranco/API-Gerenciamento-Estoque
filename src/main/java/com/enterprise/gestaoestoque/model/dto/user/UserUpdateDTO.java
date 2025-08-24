package com.enterprise.gestaoestoque.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserUpdateDTO(

        @NotBlank
        @Email
        String email
) {
}
