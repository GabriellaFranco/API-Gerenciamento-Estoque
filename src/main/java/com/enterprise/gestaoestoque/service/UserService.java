package com.enterprise.gestaoestoque.service;

import com.enterprise.gestaoestoque.exception.BusinessException;
import com.enterprise.gestaoestoque.exception.ResourceNotFoundException;
import com.enterprise.gestaoestoque.mapper.UserMapper;
import com.enterprise.gestaoestoque.model.dto.user.UserRequestDTO;
import com.enterprise.gestaoestoque.model.dto.user.UserResponseDTO;
import com.enterprise.gestaoestoque.model.dto.user.UserUpdateDTO;
import com.enterprise.gestaoestoque.model.entity.User;
import com.enterprise.gestaoestoque.repository.AuthorityRepository;
import com.enterprise.gestaoestoque.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthorityRepository authorityRepository;
    private  final PasswordEncoder passwordEncoder;

    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        var users = userRepository.findAll(pageable);
        return users.map(userMapper::toUserResponseDTO);
    }

    public UserResponseDTO getUserById(Long id) {
        return userRepository.findById(id).map(userMapper::toUserResponseDTO)
                .orElseThrow(() -> new ResourceAccessException("Usuário não encontrado: " + id));
    }

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO userDTO) {
        validateUniqueEmail(userDTO.email(), null);
        var userEntity = userMapper.toUser(userDTO);

        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        grantAuthorityByProfile(userEntity);
        userEntity.setIsActive(true);

        var userSaved = userRepository.save(userEntity);
        return userMapper.toUserResponseDTO(userSaved);
    }

    @Transactional
    public UserResponseDTO updateUserEmail(UserUpdateDTO updateDTO) {
        var user = getLoggedUser();
        if (!user.getIsActive()) {
            throw new BusinessException("Não é possível alterar o email de um usuário inativo");
        }

        validateUniqueEmail(updateDTO.email(), user.getId());
        userMapper.updateFromDTO(updateDTO, user);
        var userSaved = userRepository.save(user);
        return userMapper.toUserResponseDTO(userSaved);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        if (user.getIsActive()) {
            throw new BusinessException("Não é possível excluir um usuário ativo, desative o usuário primeiro   ");
        }
        userRepository.delete(user);
    }

    protected User getLoggedUser() {
        var user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(user instanceof UserDetails userDetails)) {
            throw new BadCredentialsException("Usuário não autenticado");
        }
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + userDetails.getUsername()));
    }

    private void validateUniqueEmail(String email, Long userId) {
        userRepository.findByEmail(email)
                .filter(user -> !user.getId().equals(userId))
                .ifPresent(user -> {
                    throw new BusinessException("Email já cadastrado: " + email);
                });
    }

    private void grantAuthorityByProfile(User user) {
        var authorityName = switch (user.getProfile()) {
            case ADMIN -> "ADMIN";
            case SUPERVISOR -> "SUPERVISOR";
            case FUNCIONARIO -> "FUNCIONARIO";
        };

        var authority = authorityRepository.findByName(authorityName)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado: " + authorityName));

        user.setAuthorities(List.of(authority));
    }
}
