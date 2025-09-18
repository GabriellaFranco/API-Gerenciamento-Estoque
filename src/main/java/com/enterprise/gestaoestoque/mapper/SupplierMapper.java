package com.enterprise.gestaoestoque.mapper;

import com.enterprise.gestaoestoque.model.dto.supplier.SupplierRequestDTO;
import com.enterprise.gestaoestoque.model.dto.supplier.SupplierResponseDTO;
import com.enterprise.gestaoestoque.model.dto.supplier.SupplierUpdateDTO;
import com.enterprise.gestaoestoque.model.entity.Supplier;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SupplierMapper {

    public Supplier toSupplier(SupplierRequestDTO supplierDTO) {
        return Supplier.builder()
                .name(supplierDTO.name())
                .cnpj(supplierDTO.cnpj())
                .contactName(supplierDTO.contactName())
                .email(supplierDTO.email())
                .phone(supplierDTO.phone())
                .build();
    }

    public SupplierResponseDTO toSupplierResponseDTO(Supplier supplier) {
        return SupplierResponseDTO.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .cnpj(supplier.getCnpj())
                .contactName(supplier.getContactName())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .isActive(supplier.getIsActive())
                .build();
    }

    public void updateFromDTO(SupplierUpdateDTO updateDTO, Supplier existingSupplier) {
        Optional.ofNullable(updateDTO.contactName()).ifPresent(existingSupplier::setContactName);
        Optional.ofNullable(updateDTO.email()).ifPresent(existingSupplier::setEmail);
        Optional.ofNullable(updateDTO.phone()).ifPresent(existingSupplier::setPhone);
    }
}
