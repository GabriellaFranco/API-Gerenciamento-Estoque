package com.enterprise.gestaoestoque.repository;

import com.enterprise.gestaoestoque.enums.LotStatus;
import com.enterprise.gestaoestoque.model.entity.Lot;
import com.enterprise.gestaoestoque.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotRepository extends JpaRepository<Lot, Long> {

    List<Lot> findByProductAndStatus(Product product, LotStatus status);

    List<Lot> findByStatus(LotStatus status);
}
