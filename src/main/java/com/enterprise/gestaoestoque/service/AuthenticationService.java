package com.enterprise.gestaoestoque.service;

import com.enterprise.gestaoestoque.exception.BusinessException;
import com.enterprise.gestaoestoque.exception.ResourceNotFoundException;
import com.enterprise.gestaoestoque.model.dto.authentication.UpdatePasswordDTO;
import com.enterprise.gestaoestoque.model.dto.authentication.UpdateUserProfileAndAuthority;
import com.enterprise.gestaoestoque.repository.AuthorityRepository;
import com.enterprise.gestaoestoque.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthorityRepository authorityRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void updatePassword(UpdatePasswordDTO updateDTO) {
        var loggedUser = userService.getLoggedUser();
        validatePasswordUpdate(updateDTO);
        loggedUser.setPassword(passwordEncoder.encode(updateDTO.newPassword()));
        userRepository.save(loggedUser);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateUserProfileAndAuthorities(Long userId, UpdateUserProfileAndAuthority updateDTO) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + userId));

        if (!user.getIsActive()) {
            throw new BusinessException("Não é possível alterar o perfil de um usuário inativo.");
        }

        var authorities = authorityRepository.findByNameIn(updateDTO.newAuthorities());
        user.setProfile(updateDTO.userProfile());
        user.setAuthorities(authorities);
        userRepository.save(user);
    }

    private void validatePasswordUpdate(UpdatePasswordDTO updateDTO) {
        var loggedUser = userService.getLoggedUser();
        if (!loggedUser.getIsActive()) {
            throw new DisabledException("Conta desativada. Contate o administrador.");
        }
        if (!passwordEncoder.matches(updateDTO.currentPassword(), loggedUser.getPassword())) {
            throw new BadCredentialsException("Senha atual incorreta");
        }
        if (passwordEncoder.matches(updateDTO.newPassword(), loggedUser.getPassword())) {
            throw new BusinessException("A nova senha deve ser diferente da atual");
        }
    }
}
