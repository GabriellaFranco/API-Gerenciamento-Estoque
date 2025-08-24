package com.enterprise.gestaoestoque.model.dto.inventory;

import com.enterprise.gestaoestoque.enums.MeasurementUnit;
import com.enterprise.gestaoestoque.enums.MovementType;
import com.enterprise.gestaoestoque.enums.UserProfile;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InventoryMovementResponseDTO(
        Long id,
        MovementType movementType,
        Double quantity,
        MeasurementUnit measurementUnit,
        LocalDateTime dateAndTime,
        Long lotId,
        UserDTO user
) {
    @Builder
    public record UserDTO(
            Long id,
            String name,
            UserProfile profile
    ) {}
}
