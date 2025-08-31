package com.enterprise.gestaoestoque.service;

import com.enterprise.gestaoestoque.exception.BusinessException;
import com.enterprise.gestaoestoque.exception.ResourceNotFoundException;
import com.enterprise.gestaoestoque.mapper.SupplierMapper;
import com.enterprise.gestaoestoque.model.dto.supplier.SupplierRequestDTO;
import com.enterprise.gestaoestoque.model.dto.supplier.SupplierResponseDTO;
import com.enterprise.gestaoestoque.model.entity.Supplier;
import com.enterprise.gestaoestoque.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierMapper supplierMapper;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier supplier;
    private SupplierRequestDTO supplierRequestDTO;
    private SupplierResponseDTO supplierResponseDTO;

    @BeforeEach
    void setup() {
        supplier = Supplier.builder()
                .id(1L)
                .name("Empresa Fornecedora")
                .cnpj("1234567890001")
                .contactName("Gabriella")
                .email("fornecedor@teste.com")
                .phone("479999995555")
                .isActive(true)
                .build();

        supplierRequestDTO = SupplierRequestDTO.builder()
                .name("Empresa Fornecedora")
                .cnpj("1234567890001")
                .contactName("Gabriella")
                .email("fornecedor@teste.com")
                .phone("479999995555")
                .build();

        supplierResponseDTO = SupplierResponseDTO.builder()
                .id(1L)
                .name("Empresa Fornecedora")
                .cnpj("1234567890001")
                .contactName("Gabriella")
                .email("fornecedor@teste.com")
                .phone("479999995555")
                .isActive(true)
                .build();

    }

    @Test
    void getAllSuppliers_WhenCalled_ShouldReturnPageOfSuppliers() {
        var pageable = PageRequest.of(1, 10);
        var supplierList = List.of(supplier);
        var suppliersPage = new PageImpl<>(supplierList, pageable, supplierList.size());

        when(supplierRepository.findAll(pageable)).thenReturn(suppliersPage);
        when(supplierMapper.toSupplierResponseDTO(supplier)).thenReturn(supplierResponseDTO);
        var result = supplierService.getAllSuppliers(pageable);

        assertThat(result)
                .isNotNull()
                .hasSize(1);
    }

    @Test
    void getSupplierById_WhenCalled_ShouldReturnSupplier() {
        when(supplierRepository.findById(supplier.getId())).thenReturn(Optional.of(supplier));
        when(supplierMapper.toSupplierResponseDTO(supplier)).thenReturn(supplierResponseDTO);

        var result = supplierService.getSupplierById(supplier.getId());

        assertThat(result)
                .isNotNull()
                .extracting(SupplierResponseDTO::id, SupplierResponseDTO::contactName)
                .containsExactly(1L, "Gabriella");
    }

    @Test
    void getSupplierById_WhenIdDoesNotExist_ShouldThrowException() {
        when(supplierRepository.findById(supplier.getId())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> supplierService.getSupplierById(supplier.getId()));
    }

    @Test
    void createSupplier_WhenCalled_ShouldCreateSupplierAndSave() {
        when(supplierRepository.findByNameIgnoreCaseAndCnpj(supplier.getName(), supplier.getCnpj())).thenReturn(Optional.empty());
        when(supplierMapper.toSupplier(supplierRequestDTO)).thenReturn(supplier);
        when(supplierRepository.save(supplier)).thenReturn(supplier);
        when(supplierMapper.toSupplierResponseDTO(supplier)).thenReturn(supplierResponseDTO);

        var result = supplierService.createSupplier(supplierRequestDTO);

        assertThat(result)
                .isNotNull()
                .extracting(SupplierResponseDTO::id, SupplierResponseDTO::contactName)
                .containsExactly(1L, "Gabriella");

    }

    @Test
    void createSupplier_WhenSupplierWithSameNameAndCnpjAlreadyExists_ShouldThrowException() {
        when(supplierRepository.findByNameIgnoreCaseAndCnpj(supplier.getName(), supplier.getCnpj())).thenReturn(Optional.of(supplier));
        assertThrows(BusinessException.class, () -> supplierService.createSupplier(supplierRequestDTO));
    }

    @Test
    void deleteSupplier_WhenCalled_ShouldDeleteSupplierAndSave() {
        when(supplierRepository.findById(supplier.getId())).thenReturn(Optional.of(supplier));
        supplier.setIsActive(false);
        supplierService.deleteSupplier(supplier.getId());

        verify(supplierRepository).findById(supplier.getId());
        verify(supplierRepository).delete(supplier);
    }

    @Test
    void deleteSupplier_WhenSupplierIsActive_ShouldThrowException() {
        when(supplierRepository.findById(supplier.getId())).thenReturn(Optional.of(supplier));
        assertThrows(BusinessException.class, () -> supplierService.deleteSupplier(supplier.getId()));
    }

}
