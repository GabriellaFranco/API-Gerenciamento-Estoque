package com.enterprise.gestaoestoque.repository;

import com.enterprise.gestaoestoque.model.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    @Override
    Page<Supplier> findAll(Pageable pageable);

    Optional<Supplier> findByNameIgnoreCaseAndCnpj(String name, String cnpj);
}
