package com.enterprise.gestaoestoque.model.dto.product;

import com.enterprise.gestaoestoque.enums.MeasurementUnit;
import com.enterprise.gestaoestoque.enums.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record ProductUpdateDTO(

        @NotBlank
        @Pattern(regexp = "^[0-9]+$", message = "Apenas números são permitidos neste campo")
        @Positive(message = "A quantidade mínima deve ser um valor positivo")
        Long minQuantity,

        @NotBlank
        ProductCategory category,

        @NotBlank
        MeasurementUnit measurementUnit
) {
}
