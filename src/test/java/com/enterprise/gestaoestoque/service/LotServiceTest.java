package com.enterprise.gestaoestoque.service;

import com.enterprise.gestaoestoque.enums.LotStatus;
import com.enterprise.gestaoestoque.enums.MeasurementUnit;
import com.enterprise.gestaoestoque.enums.MovementType;
import com.enterprise.gestaoestoque.exception.BusinessException;
import com.enterprise.gestaoestoque.exception.ResourceNotFoundException;
import com.enterprise.gestaoestoque.mapper.LotMapper;
import com.enterprise.gestaoestoque.model.dto.lot.LotRequestDTO;
import com.enterprise.gestaoestoque.model.dto.lot.LotResponseDTO;
import com.enterprise.gestaoestoque.model.entity.InventoryMovement;
import com.enterprise.gestaoestoque.model.entity.Lot;
import com.enterprise.gestaoestoque.model.entity.Product;
import com.enterprise.gestaoestoque.model.entity.Supplier;
import com.enterprise.gestaoestoque.repository.InventoryMovementRepository;
import com.enterprise.gestaoestoque.repository.LotRepository;
import com.enterprise.gestaoestoque.repository.ProductRepository;
import com.enterprise.gestaoestoque.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LotServiceTest {

    @Mock
    private LotRepository lotRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private LotMapper lotMapper;

    @InjectMocks
    private LotService lotService;

    private Lot lot;
    private Product product;
    private Supplier supplier;
    private LotResponseDTO lotResponseDTO;
    private LotRequestDTO lotRequestDTO;

    @BeforeEach
    void setup() {
        product = Product.builder()
                .id(1L)
                .name("Maçã fuji")
                .measurementUnit(MeasurementUnit.KG)
                .totalStock(50L)
                .isActive(true)
                .build();

        supplier = Supplier.builder()
                .id(1L)
                .name("Fornecedor")
                .cnpj("3983283082308")
                .isActive(true)
                .build();

        lot = Lot.builder()
                .id(1L)
                .lotCode("13HTUBDO67")
                .product(product)
                .supplier(supplier)
                .measurementUnit(MeasurementUnit.KG)
                .expirationDate(LocalDate.now().plusDays(20))
                .currentQtd(50L)
                .initialQtd(50L)
                .entryDate(LocalDate.now())
                .status(LotStatus.ATIVO)
                .build();

        lotRequestDTO = LotRequestDTO.builder()
                .productId(1L)
                .supplierId(1L)
                .initialQtd(50L)
                .measurementUnit(MeasurementUnit.KG)
                .expirationDate(LocalDate.now().plusDays(20))
                .build();

        lotResponseDTO = LotResponseDTO.builder()
                .id(1L)
                .lotCode("13HTUBDO67")
                .product(new LotResponseDTO.ProductDTO(1L, "Maçã"))
                .supplier(new LotResponseDTO.SupplierDTO(1L, "Sacolão Blumenau", "123976365563"))
                .measurementUnit(MeasurementUnit.KG)
                .expirationDate(LocalDate.now().plusDays(20))
                .currentQtd(50L)
                .initialQtd(50L)
                .entryDate(LocalDate.now())
                .status(LotStatus.ATIVO)
                .build();
    }

    @Test
    void getAllLots_WhenCalled_ShouldReturnAllLots() {
        var pageable = PageRequest.of(1, 10);
        var lotsList = List.of(lot);
        var lotPage = new PageImpl<>(lotsList, pageable, lotsList.size());

        when(lotRepository.findAll(pageable)).thenReturn(lotPage);
        when(lotMapper.toLotResponseDTO(lot)).thenReturn(lotResponseDTO);

        var result = lotService.getAllLots(pageable);

        assertThat(result)
                .isNotNull()
                .hasSize(1);
    }

    @Test
    void getLotById_WhenCalled_ShouldReturnLotObject() {
        when(lotRepository.findById(lot.getId())).thenReturn(Optional.of(lot));
        when(lotMapper.toLotResponseDTO(lot)).thenReturn(lotResponseDTO);

        var result = lotService.getLotById(lot.getId());

        assertThat(result)
                .isNotNull()
                .extracting(LotResponseDTO::measurementUnit, LotResponseDTO::status)
                .containsExactly(MeasurementUnit.KG, LotStatus.ATIVO);
    }

    @Test
    void getLotById_WhenIdDoesNotExist_ShouldThrowException() {
        when(lotRepository.findById(lot.getId())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> lotService.getLotById(lot.getId()));
    }

    @Test
    void createLot_WhenCalled_ShouldCreateAndSaveSuccessfully() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(supplierRepository.findById(supplier.getId())).thenReturn(Optional.of(supplier));
        when(lotMapper.toLot(lotRequestDTO, product, supplier)).thenReturn(lot);
        when(lotRepository.save(lot)).thenReturn(lot);
        when(lotMapper.toLotResponseDTO(lot)).thenReturn(lotResponseDTO);

        var result = lotService.createLot(lotRequestDTO);

        assertThat(result)
                .isNotNull()
                .extracting(LotResponseDTO::measurementUnit, LotResponseDTO::initialQtd)
                .containsExactly(MeasurementUnit.KG, 50L);
    }

    @Test
    void createLot_WhenProductOrSupplierAreInactive_ShouldThrowException() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(supplierRepository.findById(supplier.getId())).thenReturn(Optional.of(supplier));
        product.setIsActive(false);
        supplier.setIsActive(false);

        assertThrows(BusinessException.class, () -> lotService.createLot(lotRequestDTO));
    }

    @Test
    void updateLotStatuses_WhenLotIsExpired_ShouldUpdateStatusToVencido() {
        lot.setExpirationDate(LocalDate.now().minusDays(1));
        when(lotRepository.findByStatus(LotStatus.ATIVO)).thenReturn(List.of(lot));
        lotService.updateLotStatuses();

        assertThat(lot)
                .extracting(Lot::getStatus, Lot::getInitialQtd)
                .containsExactly(LotStatus.VENCIDO, 50L);
    }

    @Test
    void updateLotStatuses_WhenLotIsDepleted_ShouldUpdateStatusToEsgotado() {
        lot.setCurrentQtd(0L);
        when(lotRepository.findByStatus(LotStatus.ATIVO)).thenReturn(List.of(lot));
        lotService.updateLotStatuses();

        assertThat(lot)
                .extracting(Lot::getStatus, Lot::getCurrentQtd)
                .containsExactly(LotStatus.ESGOTADO, 0L);
    }

    @Test
    void updateProductTotalStock_WhenLotsAreUsed_ShouldRecalculateProductStock()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        var compra = new InventoryMovement();
        compra.setMovementType(MovementType.COMPRA);
        compra.setQuantity(5.0);

        var uso = new InventoryMovement();
        uso.setMovementType(MovementType.USO_PRODUCAO);
        uso.setQuantity(10.0);

        when(lotRepository.findByProductAndStatus(product, LotStatus.ATIVO)).thenReturn(List.of(lot));
        when(inventoryMovementRepository.findByLot(lot)).thenReturn(List.of(compra, uso));

        var method = LotService.class.getDeclaredMethod("updateProductTotalStock", Product.class);
        method.setAccessible(true);
        method.invoke(lotService, product);

        assertThat(product.getTotalStock()).isEqualTo(45L);
        verify(productRepository).save(product);
    }


    @Test
    void deleteLot_WhenCalled_ShouldDeleteSuccessfully() {
        when(lotRepository.findById(lot.getId())).thenReturn(Optional.of(lot));
        lot.setStatus(LotStatus.ESGOTADO);
        lotService.deleteLot(lot.getId());

        verify(lotRepository).findById(lot.getId());
        verify(lotRepository).delete(lot);
    }

    @Test
    void deleteLot_WhenLotHasStatusAtivo_ShouldThrowException() {
        when(lotRepository.findById(lot.getId())).thenReturn(Optional.of(lot));
        assertThrows(BusinessException.class, () -> lotService.deleteLot(lot.getId()));
    }
}
