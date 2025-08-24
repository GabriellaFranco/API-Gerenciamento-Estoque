package com.enterprise.gestaoestoque.model.dto.product;

import com.enterprise.gestaoestoque.enums.MeasurementUnit;
import com.enterprise.gestaoestoque.enums.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ProductRequestDTO(

        @NotBlank
        @Size(min = 4, max = 100, message = "O nome deve ter entre 4 e 100 caracteres")
        String name,

        @NotBlank
        ProductCategory category,

        @NotBlank
        MeasurementUnit measurementUnit,

        @NotBlank
        @Pattern(regexp = "^[0-9]+$", message = "Apenas números são permitidos neste campo")
        @Positive(message = "A quantidade mínima deve ser um valor positivo")
        Long minQuantity
) {
}
