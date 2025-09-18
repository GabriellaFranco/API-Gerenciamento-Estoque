package com.enterprise.gestaoestoque.model.dto.lot;

import com.enterprise.gestaoestoque.enums.MeasurementUnit;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record LotRequestDTO(

        @NotBlank
        Long productId,

        @NotBlank
        @Pattern(regexp = "^[0-9]+$", message = "Apenas números são permitidos neste campo")
        @Positive(message = "A quantidade inicial deve ser um valor positivo")
        Long initialQtd,

        @NotBlank
        MeasurementUnit measurementUnit,

        @NotBlank
        @Future(message = "A data de validade deve ser futura")
        LocalDate expirationDate,

        @NotBlank
        Long supplierId

) {
}
