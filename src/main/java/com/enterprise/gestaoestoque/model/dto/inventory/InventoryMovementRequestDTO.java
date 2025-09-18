package com.enterprise.gestaoestoque.model.dto.inventory;

import com.enterprise.gestaoestoque.enums.MeasurementUnit;
import com.enterprise.gestaoestoque.enums.MovementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record InventoryMovementRequestDTO (

        @NotBlank
        Long lotId,

        @NotBlank
        MovementType movementType,

        @NotBlank
        @Pattern(regexp = "^[0-9]+$", message = "Apenas números são permitidos neste campo")
        @Positive(message = "A quantidade mínima deve ser um valor positivo")
        Double quantity,

        @NotBlank
        MeasurementUnit measurementUnit

){
}
