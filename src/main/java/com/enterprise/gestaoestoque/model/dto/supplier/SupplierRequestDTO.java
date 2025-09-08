package com.enterprise.gestaoestoque.model.dto.supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record SupplierRequestDTO(

        @NotBlank
        @Size(min = 5, max = 80, message = "O nome do fornecedor deve possuir entre 6 e 80 caracteres")
        String name,

        @NotBlank
        @Pattern(
                regexp = "^\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}$",
                message = "CNPJ deve estar no formato XX.XXX.XXX/XXXX-XX"
        )
        String cnpj,

        @NotBlank
        @Pattern(regexp = "^[A-Za-zÀ-ÿ\\s]+$", message = "Apenas letras são permitidas neste campo")
        @Size(min = 6, max = 80, message = "O nome do contato deve possuir entre 6 e 80 caracteres")
        String contactName,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Pattern(
                regexp = "^\\(?\\d{2}\\)?[\\s-]?[9]?\\d{4}-?\\d{4}$",
                message = "Telefone deve estar no formato (XX) XXXXX-XXXX ou (XX) XXXX-XXXX"
        )
        String phone

) {
}
