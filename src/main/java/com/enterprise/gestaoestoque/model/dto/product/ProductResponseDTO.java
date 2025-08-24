package com.enterprise.gestaoestoque.model.dto.product;

import com.enterprise.gestaoestoque.enums.LotStatus;
import com.enterprise.gestaoestoque.enums.MeasurementUnit;
import com.enterprise.gestaoestoque.enums.ProductCategory;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record ProductResponseDTO(
        Long id,
        String name,
        ProductCategory category,
        MeasurementUnit measurementUnit,
        Boolean isActive,
        List<LotDTO> lots
) {
    @Builder
    public record LotDTO(
            String lotCode,
            LotStatus status,
            LocalDate expirationDate
    ) {}
}
