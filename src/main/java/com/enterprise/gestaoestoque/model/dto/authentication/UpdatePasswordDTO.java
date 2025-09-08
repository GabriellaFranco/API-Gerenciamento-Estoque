package com.enterprise.gestaoestoque.model.dto.authentication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePasswordDTO(

        @NotBlank
        @Size(min = 6, max = 20, message = "A senha atual deve conter entre 6 e 20 caracteres")
        String currentPassword,

        @NotBlank
        @Size(min = 6, max = 20, message = "A nova senha deve conter entre 6 e 20 caracteres")
        String newPassword
) {
}
