package com.enterprise.gestaoestoque.service;

import com.enterprise.gestaoestoque.enums.MeasurementUnit;
import com.enterprise.gestaoestoque.enums.ProductCategory;
import com.enterprise.gestaoestoque.exception.BusinessException;
import com.enterprise.gestaoestoque.exception.ResourceNotFoundException;
import com.enterprise.gestaoestoque.mapper.ProductMapper;
import com.enterprise.gestaoestoque.model.dto.product.ProductRequestDTO;
import com.enterprise.gestaoestoque.model.dto.product.ProductResponseDTO;
import com.enterprise.gestaoestoque.model.entity.Product;
import com.enterprise.gestaoestoque.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductRequestDTO productRequestDTO;
    private ProductResponseDTO productResponseDTO;

    @BeforeEach
    void setup() {
        product = Product.builder()
                .id(1L)
                .name("Maçã fuji")
                .category(ProductCategory.HORTIFRUTI)
                .minQuantity(20L)
                .measurementUnit(MeasurementUnit.UN)
                .isActive(true)
                .build();

        productRequestDTO = ProductRequestDTO.builder()
                .name("Maçã fuji")
                .category(ProductCategory.HORTIFRUTI)
                .minQuantity(20L)
                .measurementUnit(MeasurementUnit.UN)
                .build();

        productResponseDTO = ProductResponseDTO.builder()
                .id(1L)
                .name("Maçã fuji")
                .category(ProductCategory.HORTIFRUTI)
                .measurementUnit(MeasurementUnit.UN)
                .isActive(true)
                .build();
    }

    @Test
    void getAllProducts_WhenCalled_ShouldReturnPageOfProducts() {
        var pageble = PageRequest.of(1, 10);
        List<Product> listProducts = List.of(product);
        Page<Product> pageProduct = new PageImpl<>(listProducts, pageble, listProducts.size());

        when(productRepository.findAll(pageble)).thenReturn(pageProduct);
        when(productMapper.toProductResponseDTO(product)).thenReturn(productResponseDTO);

        var products = productService.getAllProducts(pageble);

        assertThat(products)
                .isNotNull()
                .hasSize(1);
    }

    @Test
    void getProductById_WhenCalled_ShouldReturnProduct() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productMapper.toProductResponseDTO(product)).thenReturn(productResponseDTO);

        var result = productService.getProductById(product.getId());

        assertThat(result)
                .isNotNull()
                .extracting(ProductResponseDTO::name, ProductResponseDTO::category)
                .containsExactly("Maçã fuji", ProductCategory.HORTIFRUTI);
    }

    @Test
    void getProductById_WhenIdDoesNotExist_ShouldThrowException() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(product.getId()));
    }

    @Test
    void createProduct_WhenCalled_ShouldCreateAndSaveProduct() {
        when(productRepository.findByNameIgnoreCase(product.getName())).thenReturn(Optional.empty());
        when(productMapper.toProduct(productRequestDTO)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toProductResponseDTO(product)).thenReturn(productResponseDTO);

        var result = productService.createProduct(productRequestDTO);

        assertThat(result)
                .isNotNull()
                .extracting(ProductResponseDTO::name, ProductResponseDTO::category)
                .containsExactly("Maçã fuji", ProductCategory.HORTIFRUTI);
    }

    @Test
    void createProduct_WhenProductAlreadyExists_ShouldThrowException() {
        when(productRepository.findByNameIgnoreCase(product.getName())).thenReturn(Optional.of(product));
        assertThrows(BusinessException.class, () -> productService.createProduct(productRequestDTO));
    }

    @Test
    void createProduct_WhenMeasurementUnitDoesntMatchProductCategory_ShouldThrowException() {
        when(productRepository.findByNameIgnoreCase(product.getName())).thenReturn(Optional.empty());

        var invalidDTO = ProductRequestDTO.builder()
                .name(productRequestDTO.name())
                .category(ProductCategory.PADARIA)
                .measurementUnit(MeasurementUnit.L)
                .build();

        assertThrows(BusinessException.class, () -> productService.createProduct(invalidDTO));
    }

    @Test
    void deleteProduct_WhenCalled_ShouldDeleteProductAndSave() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        product.setIsActive(false);
        productService.deleteProduct(product.getId());
        verify(productRepository).findById(product.getId());
        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_WhenIdDoesNotExist_ShouldThrowException() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(product.getId()));
    }

    @Test
    void deleteProduct_WhenTryingToDeleteActiveProduct_ShouldThrowException() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        assertThrows(BusinessException.class, () -> productService.deleteProduct(product.getId()));
    }
}
