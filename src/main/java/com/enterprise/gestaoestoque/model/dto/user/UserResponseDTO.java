package com.enterprise.gestaoestoque.model.dto.user;

import com.enterprise.gestaoestoque.enums.UserProfile;
import lombok.Builder;

@Builder
public record UserResponseDTO(
        Long id,
        String name,
        String email,
        UserProfile profile,
        Boolean isActive
) {
}
