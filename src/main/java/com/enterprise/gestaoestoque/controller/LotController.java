package com.enterprise.gestaoestoque.controller;

import com.enterprise.gestaoestoque.model.dto.lot.LotRequestDTO;
import com.enterprise.gestaoestoque.model.dto.lot.LotResponseDTO;
import com.enterprise.gestaoestoque.service.LotService;
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
@RequestMapping("/lots")
public class LotController {

    private final LotService lotService;

    @Operation(
            summary = "Retorna todos os lotes, em páginas com 10 objetos ordenados por id.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sucesso"),
                    @ApiResponse(responseCode = "204", description = "Nenhum registro a exibir")
            }
    )
    @GetMapping
    public ResponseEntity<Page<LotResponseDTO>> getAllLots(@PageableDefault(page = 1, size = 10, sort = "id") Pageable pageable) {
        var lots = lotService.getAllLots(pageable);
        return lots.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(lots);
    }

    @Operation(
            summary = "Retorna um produto com o id informado.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sucesso"),
                    @ApiResponse(responseCode = "404", description = "Lote não encontrado")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<LotResponseDTO> getLotById(@PathVariable Long id) {
        return ResponseEntity.ok(lotService.getLotById(id));
    }

    @Operation(
            summary = "Cria um novo lote.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Sucesso"),
                    @ApiResponse(responseCode = "400", description = "Informações inválidas")
            }
    )
    @PostMapping
    public ResponseEntity<LotResponseDTO> createLot(@Valid @RequestBody LotRequestDTO lotDTO) {
        var lot = lotService.createLot(lotDTO);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(lot.id()).toUri();
        return ResponseEntity.created(uri).body(lot);
    }

    @Operation(
            summary = "Exclui o lote com o id informado. Para chamar este endpoint" +
                    " é necessário possuir a permissão 'SUPERVISOR'.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sucesso"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão"),
                    @ApiResponse(responseCode = "404", description = "Lote não encontrado")
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<String> deleteLot(@PathVariable Long id) {
        lotService.deleteLot(id);
        return ResponseEntity.ok("Lote excluído com sucesso: " + id);
    }
}
