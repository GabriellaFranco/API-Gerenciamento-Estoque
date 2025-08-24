package com.enterprise.gestaoestoque.mapper;

import com.enterprise.gestaoestoque.model.dto.lot.LotRequestDTO;
import com.enterprise.gestaoestoque.model.dto.lot.LotResponseDTO;
import com.enterprise.gestaoestoque.model.entity.Lot;
import com.enterprise.gestaoestoque.model.entity.Product;
import com.enterprise.gestaoestoque.model.entity.Supplier;
import org.springframework.stereotype.Component;

@Component
public class LotMapper {

    public Lot toLot(LotRequestDTO lotDTO, Product product, Supplier supplier) {
        return Lot.builder()
                .currentQtd(lotDTO.initialQtd())
                .expirationDate(lotDTO.expirationDate())
                .measurementUnit(lotDTO.measurementUnit())
                .product(product)
                .supplier(supplier)
                .build();
    }

    public LotResponseDTO toLotResponseDTO(Lot lot) {
        return LotResponseDTO.builder()
                .id(lot.getId())
                .lotCode(lot.getLotCode())
                .initialQtd(lot.getInitialQtd())
                .currentQtd(lot.getCurrentQtd())
                .measurementUnit(lot.getMeasurementUnit())
                .entryDate(lot.getEntryDate())
                .expirationDate(lot.getExpirationDate())
                .status(lot.getStatus())
                .supplier(LotResponseDTO.SupplierDTO.builder()
                        .id(lot.getSupplier().getId())
                        .name(lot.getSupplier().getName())
                        .cnpj(lot.getSupplier().getCnpj())
                        .build())
                .product(LotResponseDTO.ProductDTO.builder()
                        .id(lot.getProduct().getId())
                        .name(lot.getProduct().getName())
                        .build())
                .build();
    }
}
