package com.enterprise.gestaoestoque.controller;

import com.enterprise.gestaoestoque.model.dto.inventory.InventoryMovementRequestDTO;
import com.enterprise.gestaoestoque.model.dto.inventory.InventoryMovementResponseDTO;
import com.enterprise.gestaoestoque.service.InventoryMovementService;
import com.enterprise.gestaoestoque.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("inventory-movements")
public class InventoryMovementController {

    private final InventoryMovementService inventoryMovementService;

    @Operation(
            summary = "Retorna todos as movimentações de inventário, em páginas com 10 objetos ordenados por id.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sucesso"),
                    @ApiResponse(responseCode = "204", description = "Nenhum registro a exibir")
            })
    @GetMapping
    public ResponseEntity<Page<InventoryMovementResponseDTO>> getAllInventoryMovements(@PageableDefault(page = 1, size = 10, sort = "id")
                                                                                       Pageable pageable) {

        var movements = inventoryMovementService.getAllInventoryMovements(pageable);
        return movements.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(movements);
    }

    @Operation(
            summary = "Retorna uma movimentação de inventário com o id informado.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sucesso"),
                    @ApiResponse(responseCode = "404", description = "Movimentação não encontrada")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<InventoryMovementResponseDTO> getInventoryMovementById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryMovementService.getInventoryMovementById(id));
    }

    @Operation(
            summary = "Cria uma nova movimentação de inventário.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Sucesso"),
                    @ApiResponse(responseCode = "400", description = "Informações inválidas")
            }
    )
    @PostMapping
    public ResponseEntity<InventoryMovementResponseDTO> createInventoryMovement(@Valid @RequestBody InventoryMovementRequestDTO movementDTO) {
        var movement = inventoryMovementService.createInventoryMovement(movementDTO);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(movement.id()).toUri();
        return ResponseEntity.created(uri).body(movement);
    }

    @Operation(
            summary = "Exclui a movimentação de inventário com o id informado. Para chamar este endpoint" +
                    " é necessário possuir a permissão 'SUPERVISOR'",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sucesso"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão"),
                    @ApiResponse(responseCode = "404", description = "Movimentação não encontrada")
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteInventoryTransaction(@PathVariable Long id) {
        inventoryMovementService.deleteMovement(id);
        return ResponseEntity.ok("Movimentação excluída com sucesso: " + id);
    }
}
