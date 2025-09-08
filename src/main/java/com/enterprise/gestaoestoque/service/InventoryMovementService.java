package com.enterprise.gestaoestoque.service;

import com.enterprise.gestaoestoque.enums.LotStatus;
import com.enterprise.gestaoestoque.enums.MovementType;
import com.enterprise.gestaoestoque.exception.BusinessException;
import com.enterprise.gestaoestoque.exception.ResourceNotFoundException;
import com.enterprise.gestaoestoque.mapper.InventoryMapper;
import com.enterprise.gestaoestoque.model.dto.inventory.InventoryMovementRequestDTO;
import com.enterprise.gestaoestoque.model.dto.inventory.InventoryMovementResponseDTO;
import com.enterprise.gestaoestoque.model.entity.Lot;
import com.enterprise.gestaoestoque.repository.InventoryMovementRepository;
import com.enterprise.gestaoestoque.repository.LotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class InventoryMovementService {

    private final InventoryMovementRepository inventoryMovementRepository;
    private final LotRepository lotRepository;
    private final InventoryMapper inventoryMapper;
    private final LotService lotService;
    private final UserService userService;

    public Page<InventoryMovementResponseDTO> getAllInventoryMovements(Pageable pageable) {
        var movements = inventoryMovementRepository.findAll(pageable);
        return movements.map(inventoryMapper::toInventoryMovementResponseDTO);
    }

    public InventoryMovementResponseDTO getInventoryMovementById(Long id) {
        return inventoryMovementRepository.findById(id).map(inventoryMapper::toInventoryMovementResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Movimento de estoque não encontrado: " + id));
    }

    @Transactional
    public InventoryMovementResponseDTO createInventoryMovement(InventoryMovementRequestDTO inventoryMovementDTO) {
        var user = userService.getLoggedUser();
        var lot = lotRepository.findById(inventoryMovementDTO.lotId())
                .orElseThrow(() -> new ResourceNotFoundException(("Lote não encontrado: " + inventoryMovementDTO.lotId())));
        var product = lot.getProduct();

        var inventoryMovementEntity = inventoryMapper.toInventoryMovement(inventoryMovementDTO, user, lot);
        inventoryMovementEntity.setMeasurementUnit(lot.getMeasurementUnit());
        inventoryMovementEntity.setDateAndTime(LocalDateTime.now());
        var inventoryMovementSaved = inventoryMovementRepository.save(inventoryMovementEntity);

        applyWithdrawalToLot(lot, inventoryMovementDTO.quantity().longValue());
        lotService.updateProductTotalStock(product);

        return inventoryMapper.toInventoryMovementResponseDTO(inventoryMovementSaved);
    }

    @Transactional
    @PreAuthorize("HasRole('SUPERVISOR')")
    public void deleteMovement(Long movementId) {
        var movement = inventoryMovementRepository.findById(movementId)
                .orElseThrow(() -> new ResourceNotFoundException("Movimento não encontrado: " + movementId));

        var lot = movement.getLot();
        var product = lot.getProduct();

        if (movement.getMovementType() == MovementType.USO_PRODUCAO || movement.getMovementType() == MovementType.PERDA) {
            revertWithdrawalFromLot(lot, movement.getQuantity().longValue());
        }

        inventoryMovementRepository.delete(movement);
        lotService.updateProductTotalStock(product);
    }

    private void applyWithdrawalToLot(Lot lot, long quantity) {
        if (quantity <= 0) {
            throw new BusinessException("Quantidade inválida");
        }
        if (quantity > lot.getCurrentQtd()) {
            throw new BusinessException("Quantidade maior que a disponível no lote.");
        }

        var newQty = lot.getCurrentQtd() - quantity;
        lot.setCurrentQtd(newQty);

        if (newQty == 0 && lot.getStatus() != LotStatus.VENCIDO) {
            lot.setStatus(LotStatus.ESGOTADO);
        }
        lotRepository.save(lot);
    }

    private void revertWithdrawalFromLot(Lot lot, long quantity) {
        long newQty = lot.getCurrentQtd() + quantity;
        lot.setCurrentQtd(newQty);

        if (lot.getStatus() == LotStatus.ESGOTADO
                && newQty > 0
                && LocalDate.now().isBefore(lot.getExpirationDate())) {
            lot.setStatus(LotStatus.ATIVO);
        }
        lotRepository.save(lot);
    }
}
