package com.enterprise.gestaoestoque.model.dto.lot;

import com.enterprise.gestaoestoque.enums.LotStatus;
import com.enterprise.gestaoestoque.enums.MeasurementUnit;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record LotResponseDTO(
        Long id,
        String lotCode,
        Long initialQtd,
        Long currentQtd,
        MeasurementUnit measurementUnit,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
        LocalDate entryDate,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
        LocalDate expirationDate,

        LotStatus status,
        SupplierDTO supplier,
        ProductDTO product

) {
    @Builder
    public record SupplierDTO(
            Long id,
            String name,
            String cnpj
    ) {}

    @Builder
    public record ProductDTO(
            Long id,
            String name
    ) {}

}
