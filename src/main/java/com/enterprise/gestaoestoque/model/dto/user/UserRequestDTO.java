package com.enterprise.gestaoestoque.model.dto.user;

import com.enterprise.gestaoestoque.enums.UserProfile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserRequestDTO(

        @NotBlank
        @Pattern(regexp = "^[A-Za-zÀ-ÿ\\s]+$", message = "Apenas letras são permitidas neste campo")
        @Size(min = 6, max = 80, message = "O nome deve ter entre 6 e 80 caracteres")
        String name,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 6, max = 20, message = "A senha deve conter entre 6 e 20 caracteres")
        String password,

        @NotBlank
        UserProfile profile

) {
}
