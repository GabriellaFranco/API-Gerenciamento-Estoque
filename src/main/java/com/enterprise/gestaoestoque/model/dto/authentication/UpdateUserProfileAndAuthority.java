package com.enterprise.gestaoestoque.model.dto.authentication;

import com.enterprise.gestaoestoque.enums.UserProfile;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UpdateUserProfileAndAuthority(

        @NotBlank
        UserProfile userProfile,

        @NotBlank
        List<String> newAuthorities
        ) {
}
