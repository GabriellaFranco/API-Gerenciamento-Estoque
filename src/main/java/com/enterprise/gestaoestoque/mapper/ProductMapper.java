package com.enterprise.gestaoestoque.mapper;

import com.enterprise.gestaoestoque.model.dto.product.ProductRequestDTO;
import com.enterprise.gestaoestoque.model.dto.product.ProductResponseDTO;
import com.enterprise.gestaoestoque.model.dto.product.ProductUpdateDTO;
import com.enterprise.gestaoestoque.model.entity.Product;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProductMapper {

    public Product toProduct(ProductRequestDTO productDTO) {
        return Product.builder()
                .name(productDTO.name())
                .category(productDTO.category())
                .measurementUnit(productDTO.measurementUnit())
                .minQuantity(productDTO.minQuantity())
                .build();
    }

    public ProductResponseDTO toProductResponseDTO(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .measurementUnit(product.getMeasurementUnit())
                .totalStock(product.getTotalStock())
                .isActive(product.getIsActive())
                .lots(product.getLots().stream().map(lot -> ProductResponseDTO.LotDTO.builder()
                        .lotCode(lot.getLotCode())
                        .status(lot.getStatus())
                        .expirationDate(lot.getExpirationDate())
                        .build()).toList())
                .build();
    }

    public void updateFromDTO(ProductUpdateDTO updateDTO, Product existingProduct) {
        Optional.ofNullable(updateDTO.minQuantity()).ifPresent(existingProduct::setMinQuantity);
        Optional.ofNullable(updateDTO.category()).ifPresent(existingProduct::setCategory);
        Optional.ofNullable(updateDTO.measurementUnit()).ifPresent(existingProduct::setMeasurementUnit);
    }
}
