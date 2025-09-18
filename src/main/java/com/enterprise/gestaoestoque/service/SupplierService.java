package com.enterprise.gestaoestoque.service;

import com.enterprise.gestaoestoque.exception.BusinessException;
import com.enterprise.gestaoestoque.exception.ResourceNotFoundException;
import com.enterprise.gestaoestoque.mapper.SupplierMapper;
import com.enterprise.gestaoestoque.model.dto.supplier.SupplierRequestDTO;
import com.enterprise.gestaoestoque.model.dto.supplier.SupplierResponseDTO;
import com.enterprise.gestaoestoque.model.dto.supplier.SupplierUpdateDTO;
import com.enterprise.gestaoestoque.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public Page<SupplierResponseDTO> getAllSuppliers(Pageable pageable) {
        var suppliers = supplierRepository.findAll(pageable);
        return suppliers.map(supplierMapper::toSupplierResponseDTO);
    }

    public SupplierResponseDTO getSupplierById(Long id) {
        return supplierRepository.findById(id).map(supplierMapper::toSupplierResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor não encontrado: " + id));
    }

    @Transactional
    public SupplierResponseDTO createSupplier(SupplierRequestDTO supplierDTO) {
        validateUniqueSupplier(supplierDTO.name(), supplierDTO.cnpj());
        var supplierEntity = supplierMapper.toSupplier(supplierDTO);
        supplierEntity.setIsActive(true);

        var supplierSaved = supplierRepository.save(supplierEntity);
        return supplierMapper.toSupplierResponseDTO(supplierSaved);
    }

    @Transactional
    public SupplierResponseDTO updateSupplier(SupplierUpdateDTO updateDTO, Long id) {
        var supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor não encontrado: " + id));
        validateIfSupplierIsActiveBeforeUpdate(supplier.getIsActive());

        supplierMapper.updateFromDTO(updateDTO, supplier);
        var supplierSaved = supplierRepository.save(supplier);
        return supplierMapper.toSupplierResponseDTO(supplierSaved);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public void deleteSupplier(Long id) {
        var supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor não encontrado: " + id));
        validateIfSupplierIsActiveBeforeDelete(supplier.getIsActive());
        supplierRepository.delete(supplier);
    }

    private void validateUniqueSupplier(String name, String cnpj) {
        var supplier = supplierRepository.findByNameIgnoreCaseAndCnpj(name, cnpj);
        if (supplier.isPresent()) {
            throw new BusinessException("Fornecedor já cadastrado no sistema: " + name);
        }
    }

    private void validateIfSupplierIsActiveBeforeUpdate(Boolean isActive) {
        if (!isActive) {
            throw new BusinessException("Não é possível atualizar as informações de um supervisor inativo");
        }
    }

    private void validateIfSupplierIsActiveBeforeDelete(Boolean isActive) {
        if (isActive) {
            throw new BusinessException("Não é possível excluir um fornecedor ativo, desative-o primeiro");
        }
    }
}
