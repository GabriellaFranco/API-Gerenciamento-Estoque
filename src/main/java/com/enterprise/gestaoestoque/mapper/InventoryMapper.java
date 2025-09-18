package com.enterprise.gestaoestoque.mapper;

import com.enterprise.gestaoestoque.model.dto.inventory.InventoryMovementRequestDTO;
import com.enterprise.gestaoestoque.model.dto.inventory.InventoryMovementResponseDTO;
import com.enterprise.gestaoestoque.model.entity.InventoryMovement;
import com.enterprise.gestaoestoque.model.entity.Lot;
import com.enterprise.gestaoestoque.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public InventoryMovement toInventoryMovement(InventoryMovementRequestDTO inventoryDTO, User user, Lot lot) {
        return InventoryMovement.builder()
                .movementType(inventoryDTO.movementType())
                .quantity(inventoryDTO.quantity())
                .measurementUnit(inventoryDTO.measurementUnit())
                .lot(lot)
                .user(user)
                .build();
    }

    public InventoryMovementResponseDTO toInventoryMovementResponseDTO(InventoryMovement inventoryMovement) {
        return InventoryMovementResponseDTO.builder()
                .id(inventoryMovement.getId())
                .movementType(inventoryMovement.getMovementType())
                .quantity(inventoryMovement.getQuantity())
                .measurementUnit(inventoryMovement.getMeasurementUnit())
                .dateAndTime(inventoryMovement.getDateAndTime())
                .lot(InventoryMovementResponseDTO.LotDTO.builder()
                        .lotCode(inventoryMovement.getLot().getLotCode())
                        .status(inventoryMovement.getLot().getStatus())
                        .build())
                .user(InventoryMovementResponseDTO.UserDTO.builder()
                        .id(inventoryMovement.getUser().getId())
                        .name(inventoryMovement.getUser().getName())
                        .profile(inventoryMovement.getUser().getProfile())
                        .build())
                .build();
    }
}
