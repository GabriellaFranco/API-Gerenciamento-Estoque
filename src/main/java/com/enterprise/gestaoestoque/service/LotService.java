package com.enterprise.gestaoestoque.service;

import com.enterprise.gestaoestoque.enums.LotStatus;
import com.enterprise.gestaoestoque.exception.BusinessException;
import com.enterprise.gestaoestoque.exception.ResourceNotFoundException;
import com.enterprise.gestaoestoque.mapper.LotMapper;
import com.enterprise.gestaoestoque.model.dto.lot.LotRequestDTO;
import com.enterprise.gestaoestoque.model.dto.lot.LotResponseDTO;
import com.enterprise.gestaoestoque.model.entity.Lot;
import com.enterprise.gestaoestoque.model.entity.Product;
import com.enterprise.gestaoestoque.repository.InventoryMovementRepository;
import com.enterprise.gestaoestoque.repository.LotRepository;
import com.enterprise.gestaoestoque.repository.ProductRepository;
import com.enterprise.gestaoestoque.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Service
public class LotService {

    private final LotRepository lotRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final LotMapper lotMapper;
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public Page<LotResponseDTO> getAllLots(Pageable pageable) {
        var lots = lotRepository.findAll(pageable);
        return lots.map(lotMapper::toLotResponseDTO);
    }

    public LotResponseDTO getLotById(Long id) {
        return lotRepository.findById(id)
                .map(lotMapper::toLotResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Lote não encontrado: " + id));
    }

    @Transactional
    public LotResponseDTO createLot(LotRequestDTO lotDTO) {
        var supplier = supplierRepository.findById(lotDTO.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor não encontrado: " + lotDTO.supplierId()));
        var product = productRepository.findById(lotDTO.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + lotDTO.productId()));

        validadeIfProductAndSupplierAreActive(product.getIsActive(), supplier.getIsActive());

        var lotEntity = lotMapper.toLot(lotDTO, product, supplier);
        lotEntity.setLotCode(generateLotCode());
        lotEntity.setMeasurementUnit(product.getMeasurementUnit());
        lotEntity.setEntryDate(LocalDate.now());
        lotEntity.setStatus(LotStatus.ATIVO);

        var lotSaved = lotRepository.save(lotEntity);
        updateProductTotalStock(lotSaved.getProduct());
        return lotMapper.toLotResponseDTO(lotSaved);
    }

    @Transactional
    @PreAuthorize("hasRole('SUPERVISOR')")
    public void deleteLot(Long id) {
        var lot = lotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lote não encontrado: " + id));
        validateLotDelete(lot.getStatus());

        lotRepository.delete(lot);
        updateProductTotalStock(lot.getProduct());
    }

    @Transactional
    public void updateProductTotalStock(Product product) {
        long total = lotRepository.findByProductAndStatus(product, LotStatus.ATIVO)
                .stream()
                .mapToLong(Lot::getCurrentQtd)
                .sum();

        product.setTotalStock(total);
        productRepository.save(product);
    }

    @Transactional
    @Scheduled(cron = "0 */2 * * * *") // A cada 2 minutos
    public void updateLotStatuses() {
        var lots = lotRepository.findByStatus(LotStatus.ATIVO);
        for (Lot lot : lots) {
            boolean statusChanged = false;

            if (LocalDate.now().isAfter(lot.getExpirationDate()) && lot.getStatus() != LotStatus.VENCIDO) {
                lot.setStatus(LotStatus.VENCIDO);
                statusChanged = true;
            } else if (lot.getCurrentQtd() == 0 && lot.getStatus() != LotStatus.ESGOTADO) {
                lot.setStatus(LotStatus.ESGOTADO);
                statusChanged = true;
            }

            if (statusChanged) {
                lotRepository.save(lot);
                updateProductTotalStock(lot.getProduct());
            }
        }
    }

    private static String generateLotCode() {
        var sb = new StringBuilder(15);
        var random = ThreadLocalRandom.current();
        for (int i = 0; i < 15; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    private void validateLotDelete(LotStatus status) {
        if (status.equals(LotStatus.ATIVO)) {
            throw new BusinessException("Só é possível excluir lotes com status ESGOTADO ou VENCIDO");
        }
    }

    private void validadeIfProductAndSupplierAreActive(boolean productIsActive, boolean supplierIsActive) {
        if (!productIsActive) {
            throw new BusinessException("Produto inativo no sistema, não foi possível criar lote");
        }
        if (!supplierIsActive) {
            throw new BusinessException("Fornecedor inativo no sistema, não foi possível criar lote");
        }
    }
}
