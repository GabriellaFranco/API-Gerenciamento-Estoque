package com.enterprise.gestaoestoque.mapper;

import com.enterprise.gestaoestoque.model.dto.user.UserRequestDTO;
import com.enterprise.gestaoestoque.model.dto.user.UserResponseDTO;
import com.enterprise.gestaoestoque.model.dto.user.UserUpdateDTO;
import com.enterprise.gestaoestoque.model.entity.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserMapper {

    public User toUser(UserRequestDTO userDTO) {
        return User.builder()
                .name(userDTO.name())
                .password(userDTO.password())
                .email(userDTO.email())
                .profile(userDTO.profile())
                .build();
    }

    public UserResponseDTO toUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profile(user.getProfile())
                .isActive(user.getIsActive())
                .build();
    }

    public void updateFromDTO(UserUpdateDTO updateDTO, User existingUser) {
        Optional.ofNullable(updateDTO.email()).ifPresent(existingUser::setEmail);
    }
}
