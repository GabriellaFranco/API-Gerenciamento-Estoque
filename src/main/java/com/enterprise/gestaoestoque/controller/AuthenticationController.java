package com.enterprise.gestaoestoque.controller;

import com.enterprise.gestaoestoque.configuration.jwt.JWTUtil;
import com.enterprise.gestaoestoque.model.dto.authentication.LoginRequestDTO;
import com.enterprise.gestaoestoque.model.dto.authentication.LoginResponseDTO;
import com.enterprise.gestaoestoque.model.dto.authentication.UpdatePasswordDTO;
import com.enterprise.gestaoestoque.model.dto.authentication.UpdateUserProfileAndAuthority;
import com.enterprise.gestaoestoque.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final AuthenticationService authenticationService;
    private final JWTUtil jwtUtil;

    @Operation(
            summary = "Altera a senha do usuário autenticado",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos ou senha atual incorreta"),
                    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
            }
    )
    @PatchMapping("/update-password")
    public ResponseEntity<String> updatePassword(@Valid @RequestBody UpdatePasswordDTO updateDTO) {
        authenticationService.updatePassword(updateDTO);
        return ResponseEntity.ok("Senha alterada com sucesso");
    }

    @Operation(
            summary = "Atualiza o perfil e as autoridades de um usuário. Para chamar este endpoint é necessário possuir a" +
                    "permissão 'ADMIN'",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                    @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
                    @ApiResponse(responseCode = "403", description = "Usuário autenticado sem permissão de administrador"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
            }
    )
    @PatchMapping("/usuarios/{id}/perfil-autoridades")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateUserProfileAndAuthorities(@PathVariable Long id,
                                                                  @Valid @RequestBody UpdateUserProfileAndAuthority updateDTO) {

        authenticationService.updateUserProfileAndAuthorities(id, updateDTO);
        return ResponseEntity.ok("Perfil de usuário e autoridade atualizados com sucesso");
    }

    @Operation(
            summary = "Autentica o usuário e retorna um token JWT",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(), loginRequest.password()
                )
        );

        var authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority).toList();

        var token = jwtUtil.generateToken(authentication.getName(), authorities);

        return ResponseEntity.ok(new LoginResponseDTO("Login successful", token));
    }
}
