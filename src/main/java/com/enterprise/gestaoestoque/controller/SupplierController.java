package com.enterprise.gestaoestoque.controller;

import com.enterprise.gestaoestoque.model.dto.supplier.SupplierRequestDTO;
import com.enterprise.gestaoestoque.model.dto.supplier.SupplierResponseDTO;
import com.enterprise.gestaoestoque.model.dto.supplier.SupplierUpdateDTO;
import com.enterprise.gestaoestoque.service.SupplierService;
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
@RequestMapping("/suppliers")
public class SupplierController {

    private SupplierService supplierService;

    @Operation(
            summary = "Retorna todos os fornecedores, em páginas com 10 objetos ordenados por id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sucesso"),
                    @ApiResponse(responseCode = "204", description = "Nenhum registro a exibir")
            }
    )
    @GetMapping
    public ResponseEntity<Page<SupplierResponseDTO>> getAllSupplier(@PageableDefault(page = 1, size = 10, sort = "id" ) Pageable pageable) {
        var suppliers = supplierService.getAllSuppliers(pageable);
        return suppliers.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(suppliers);
    }

    @Operation(
            summary = "Retorna um fornecedor com o id informado",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sucesso"),
                    @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado")
            }
    )
    @GetMapping("/id")
    public ResponseEntity<SupplierResponseDTO> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @Operation(
            summary = "Cria um novo fornecedor",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Sucesso"),
                    @ApiResponse(responseCode = "400", description = "Informações inválidas")
            }
    )
    @PostMapping
    public ResponseEntity<SupplierResponseDTO> createSupplier(@Valid @RequestBody SupplierRequestDTO supplierDTO) {
        var supplier = supplierService.createSupplier(supplierDTO);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(supplier.id()).toUri();
        return ResponseEntity.created(uri).body(supplier);
    }

    @Operation(
            summary = "Atualiza o fornecedor com o id correspondente",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                    @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado")
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponseDTO> updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierUpdateDTO updateDTO) {
        return ResponseEntity.ok(supplierService.updateSupplier(updateDTO, id));
    }

    @Operation(
            summary = "Exclui o fornecedor com o id informado. Para chamar este endpoint" +
                    " é necessário possuir a permissão 'SUPERVISOR'",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sucesso"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão"),
                    @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado")
            }
    )
    @DeleteMapping("/id")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<String> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok("Fornecedor excluído com sucesso: " + id);
    }
}
