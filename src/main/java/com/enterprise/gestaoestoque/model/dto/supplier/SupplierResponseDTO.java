package com.enterprise.gestaoestoque.model.dto.supplier;

import lombok.Builder;

@Builder
public record SupplierResponseDTO(
        Long id,
        String name,
        String cnpj,
        String contactName,
        String email,
        String phone,
        Boolean isActive
) {
}
