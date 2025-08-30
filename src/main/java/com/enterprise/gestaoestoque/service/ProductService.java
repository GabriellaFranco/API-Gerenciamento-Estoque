package com.enterprise.gestaoestoque.service;

import com.enterprise.gestaoestoque.enums.MeasurementUnit;
import com.enterprise.gestaoestoque.enums.ProductCategory;
import com.enterprise.gestaoestoque.exception.BusinessException;
import com.enterprise.gestaoestoque.exception.ResourceNotFoundException;
import com.enterprise.gestaoestoque.mapper.ProductMapper;
import com.enterprise.gestaoestoque.model.dto.product.ProductRequestDTO;
import com.enterprise.gestaoestoque.model.dto.product.ProductResponseDTO;
import com.enterprise.gestaoestoque.model.dto.product.ProductUpdateDTO;
import com.enterprise.gestaoestoque.model.entity.Product;
import com.enterprise.gestaoestoque.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
        var products = productRepository.findAll(pageable);
        return products.map(productMapper::toProductResponseDTO);
    }

    public ProductResponseDTO getProductById(Long id) {
        return productRepository.findById(id).map(productMapper::toProductResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));
    }

    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO productDTO) {
        validateUniqueProduct(productDTO.name());
        validateProductMeasurementUnit(productDTO.category(), productDTO.measurementUnit());
        var productEntity = productMapper.toProduct(productDTO);

        productEntity.setIsActive(true);
        var productSaved  = productRepository.save(productEntity);
        return productMapper.toProductResponseDTO(productSaved);
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductUpdateDTO updateDTO) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));

        validateIfProductIsActiveBeforeUpdate(product);
        validateProductMeasurementUnit(product.getCategory(), product.getMeasurementUnit());
        productMapper.updateFromDTO(updateDTO, product);

        var productSaved = productRepository.save(product);
        return productMapper.toProductResponseDTO(productSaved);
    }

    @Transactional
    @PreAuthorize("hasRole('SUPERVISOR')")
    public void deleteProduct(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));
        validateIfProductIsActiveBeforeDelete(product.getIsActive());
        productRepository.delete(product);
    }

    private void validateUniqueProduct(String name) {
        var product = productRepository.findByNameIgnoreCase(name);
        if (product.isPresent()) {
            throw new BusinessException("Já existe um produto cadastrado com este nome: " + name);
        }
    }

    private void validateIfProductIsActiveBeforeDelete(Boolean isActive) {
        if (isActive) {
            throw new BusinessException("O produto está ativo, é necessário desativá-lo antes de realizar esta operação");
        }
    }

    private void validateIfProductIsActiveBeforeUpdate(Product product) {
        if (!product.getIsActive()) {
            throw new BusinessException("Não é possível atualizar as informações de um produto que não está ativo");
        }
    }

    private void validateProductMeasurementUnit(ProductCategory category, MeasurementUnit measurementUnit) {
        boolean hortifruti = category.equals(ProductCategory.HORTIFRUTI);
        boolean kg = measurementUnit.equals(MeasurementUnit.KG);
        boolean g = measurementUnit.equals(MeasurementUnit.G);
        boolean un = measurementUnit.equals(MeasurementUnit.UN);

        if (hortifruti && !(kg || g || un)) {
            throw new BusinessException("Produtos da categoria HORTIFRUTI devem possuir uma das seguintes " +
                    "unidades de medida: KG, G ou UN");
        }

        boolean padaria = category.equals(ProductCategory.PADARIA);
        boolean duzia = measurementUnit.equals(MeasurementUnit.DUZIA);
        boolean ml = measurementUnit.equals(MeasurementUnit.ML);
        boolean caixa = measurementUnit.equals(MeasurementUnit.CAIXA);

        if (padaria && !(kg || g || un || duzia || caixa || ml)) {
            throw new BusinessException("Produtos da categoria PADARIA devem possuir uma das seguintes " +
                    "unidades de medida: KG, G, UN, DUZIA, CAIXA ou ML");
        }

        boolean friosLaticinios =  category.equals(ProductCategory.FRIOS_E_LATICINIOS);
        boolean l = measurementUnit.equals(MeasurementUnit.L);

        if (friosLaticinios && !(kg || g || un || l || ml || caixa || duzia)) {
            throw new BusinessException("Produtos da categoria FRIOS E LATICÍNIOS devem possuir uma das seguintes " +
                    "unidades de medida: KG, G, UN, DUZIA, CAIXA, L ou ML");
        }

        boolean carnesAves = category.equals(ProductCategory.CARNES_E_AVES);

        if (carnesAves && !(kg || g)) {
            throw new BusinessException("Produtos da categoria CARNES E AVES devem possuir uma das seguintes " +
                    "unidades de medida: KG ou G");
        }

        boolean bebidas = category.equals(ProductCategory.BEBIDAS);
        boolean fardo = measurementUnit.equals(MeasurementUnit.FARDO);
        boolean garrafa = measurementUnit.equals(MeasurementUnit.GARRAFA);
        boolean lata = measurementUnit.equals(MeasurementUnit.LATA);

        if (bebidas && !(un || garrafa || fardo || lata)) {
            throw new BusinessException("Produtos da categoria BEBIDAS devem possuir uma das seguintes " +
                    "unidades de medida: UN, GARRAFA, LATA ou FARDO");
        }

        boolean naoPereciveis = category.equals(ProductCategory.NAO_PERECIVEIS);

        if (naoPereciveis && !(kg || g || un || lata || caixa)) {
            throw new BusinessException("Produtos da categoria NÃO PERECÍVEIS devem possuir uma das seguintes " +
                    "unidades de medida: KG, G, UN, LATA ou CAIXA");
        }

        boolean congelados = category.equals(ProductCategory.CONGELADOS);

        if (congelados && !(kg || g || l || un || caixa)) {
            throw new BusinessException("Produtos da categoria CONGELADOS devem possuir uma das seguintes " +
                    "unidades de medida: KG, G, L, UN ou CAIXA");
        }

    }

}
