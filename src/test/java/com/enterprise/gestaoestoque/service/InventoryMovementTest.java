package com.enterprise.gestaoestoque.service;

import com.enterprise.gestaoestoque.enums.*;
import com.enterprise.gestaoestoque.exception.ResourceNotFoundException;
import com.enterprise.gestaoestoque.mapper.InventoryMapper;
import com.enterprise.gestaoestoque.model.dto.inventory.InventoryMovementRequestDTO;
import com.enterprise.gestaoestoque.model.dto.inventory.InventoryMovementResponseDTO;
import com.enterprise.gestaoestoque.model.entity.*;
import com.enterprise.gestaoestoque.repository.InventoryMovementRepository;
import com.enterprise.gestaoestoque.repository.LotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InventoryMovementTest {

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private LotRepository lotRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private LotService lotService;

    @Mock
    private UserService userService;

    @InjectMocks
    private InventoryMovementService inventoryMovementService;

    private InventoryMovement inventoryMovement;
    private InventoryMovementRequestDTO inventoryMovementRequestDTO;
    private InventoryMovementResponseDTO inventoryMovementResponseDTO;
    private Lot lot;
    private Product product;
    private User user;

    @BeforeEach
    void setup() {
        user = userService.getLoggedUser();

        product = Product.builder()
                .id(1L)
                .name("Maçã")
                .measurementUnit(MeasurementUnit.KG)
                .totalStock(50L)
                .category(ProductCategory.HORTIFRUTI)
                .isActive(true)
                .build();

        lot = Lot.builder()
                .id(1L)
                .product(product)
                .supplier(new Supplier())
                .initialQtd(100L)
                .currentQtd(100L)
                .expirationDate(LocalDate.now())
                .measurementUnit(product.getMeasurementUnit())
                .status(LotStatus.ATIVO)
                .build();

        inventoryMovement = InventoryMovement.builder()
                .id(1L)
                .user(user)
                .lot(lot)
                .movementType(MovementType.USO_PRODUCAO)
                .quantity(50.0)
                .build();

        inventoryMovementRequestDTO = InventoryMovementRequestDTO.builder()
                .lotId(lot.getId())
                .movementType(MovementType.USO_PRODUCAO)
                .quantity(50.0)
                .build();

        inventoryMovementResponseDTO = InventoryMovementResponseDTO.builder()
                .id(1L)
                .user(InventoryMovementResponseDTO.UserDTO.builder()
                        .id(2L)
                        .name("Gabriella")
                        .profile(UserProfile.ADMIN)
                        .build())
                .lot(InventoryMovementResponseDTO.LotDTO.builder()
                        .lotCode("YUBDW134RYG")
                        .status(LotStatus.ATIVO)
                        .build())
                .movementType(MovementType.USO_PRODUCAO)
                .quantity(50.0)
                .build();
    }

    @Test
    void getAllInventoryMovements_WhenCalled_ShouldReturnAllInventoryMovements() {
        var pageable = PageRequest.of(1, 10);
        var movementList = List.of(inventoryMovement);
        var movementPage = new PageImpl<>(movementList, pageable, movementList.size());

        when(inventoryMovementRepository.findAll(pageable)).thenReturn(movementPage);
        when(inventoryMapper.toInventoryMovementResponseDTO(inventoryMovement)).thenReturn(inventoryMovementResponseDTO);

        var result = inventoryMovementService.getAllInventoryMovements(pageable);

        assertThat(result)
                .isNotNull()
                .hasSize(1);
    }

    @Test
    void getInventoryMovementById_WhenCalled_ShouldReturnInventoryMovement() {
        when(inventoryMovementRepository.findById(inventoryMovement.getId())).thenReturn(Optional.of(inventoryMovement));
        when(inventoryMapper.toInventoryMovementResponseDTO(inventoryMovement)).thenReturn(inventoryMovementResponseDTO);

        var result = inventoryMovementService.getInventoryMovementById(inventoryMovement.getId());

        assertThat(result)
                .isNotNull()
                .extracting(InventoryMovementResponseDTO::movementType, InventoryMovementResponseDTO::quantity)
                .containsExactly(MovementType.USO_PRODUCAO, 50.0);
    }

    @Test
    void getInventoryMovementById_WhenIdDoesNotExist_ShouldThrowException() {
        when(inventoryMovementRepository.findById(inventoryMovement.getId())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> inventoryMovementService.getInventoryMovementById(inventoryMovement.getId()));
    }

    @Test
    void createInventoryMovement_WhenCalled_ShouldCreateAndSaveSuccessfully() {
        when(userService.getLoggedUser()).thenReturn(user);
        when(lotRepository.findById(lot.getId())).thenReturn(Optional.of(lot));
        when(inventoryMapper.toInventoryMovement(inventoryMovementRequestDTO, user, lot)).thenReturn(inventoryMovement);
        when(inventoryMovementRepository.save(inventoryMovement)).thenReturn(inventoryMovement);
        when(inventoryMapper.toInventoryMovementResponseDTO(inventoryMovement)).thenReturn(inventoryMovementResponseDTO);

        var result = inventoryMovementService.createInventoryMovement(inventoryMovementRequestDTO);

        assertThat(lot.getCurrentQtd()).isEqualTo(50L);
        assertThat(result).isEqualTo(inventoryMovementResponseDTO);
        verify(inventoryMovementRepository).save(inventoryMovement);
        verify(lotService).updateProductTotalStock(lot.getProduct());

    }

    @Test
    void deleteInventoryMovement_WhenCalled_ShouldDeleteSuccessfully() {
        when(inventoryMovementRepository.findById(inventoryMovement.getId())).thenReturn(Optional.of(inventoryMovement));
        inventoryMovementService.deleteMovement(inventoryMovement.getId());

        verify(inventoryMovementRepository).findById(inventoryMovement.getId());
        verify(inventoryMovementRepository).delete(inventoryMovement);
    }

    @Test
    void deleteInventoryMovement_WhenMovementIsUsoProducaoOrPerda_ShouldRevertAndDelete() {
        lot.setCurrentQtd(50L);
        when(inventoryMovementRepository.findById(inventoryMovement.getId())).thenReturn(Optional.of(inventoryMovement));
        inventoryMovementService.deleteMovement(inventoryMovement.getId());

        verify(inventoryMovementRepository).delete(inventoryMovement);
        verify(lotService).updateProductTotalStock(lot.getProduct());
        assertThat(lot.getCurrentQtd()).isEqualTo(100L);
    }

    @Test
    void deleteInventoryMovement_WhenIdDoesNotExist_ShouldThrowException() {
        when(inventoryMovementRepository.findById(inventoryMovement.getId())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> inventoryMovementService.deleteMovement(inventoryMovement.getId()));
    }
}
