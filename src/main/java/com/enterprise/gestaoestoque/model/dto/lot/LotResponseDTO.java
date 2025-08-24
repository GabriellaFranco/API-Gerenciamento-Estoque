package com.enterprise.gestaoestoque.model.dto.lot;

import com.enterprise.gestaoestoque.enums.LotStatus;
import com.enterprise.gestaoestoque.enums.MeasurementUnit;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record LotResponseDTO(
        Long id,
        String lotCode,
        Long initialQtd,
        Long currentQtd,
        MeasurementUnit measurementUnit,
        LocalDate entryDate,
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
