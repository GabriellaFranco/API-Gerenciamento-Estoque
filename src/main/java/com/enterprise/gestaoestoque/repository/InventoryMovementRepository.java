package com.enterprise.gestaoestoque.repository;

import com.enterprise.gestaoestoque.model.entity.InventoryMovement;
import com.enterprise.gestaoestoque.model.entity.Lot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement> findByLot(Lot lot);
}
