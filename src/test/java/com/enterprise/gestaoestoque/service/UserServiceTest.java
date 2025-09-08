package com.enterprise.gestaoestoque.service;

import com.enterprise.gestaoestoque.enums.UserProfile;
import com.enterprise.gestaoestoque.exception.BusinessException;
import com.enterprise.gestaoestoque.exception.ResourceNotFoundException;
import com.enterprise.gestaoestoque.mapper.UserMapper;
import com.enterprise.gestaoestoque.model.dto.user.UserRequestDTO;
import com.enterprise.gestaoestoque.model.dto.user.UserResponseDTO;
import com.enterprise.gestaoestoque.model.entity.Authority;
import com.enterprise.gestaoestoque.model.entity.User;
import com.enterprise.gestaoestoque.repository.AuthorityRepository;
import com.enterprise.gestaoestoque.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthorityRepository authorityRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private Authority authority;
    private UserRequestDTO userRequestDTO;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1L)
                .name("Gabriella")
                .email("dev@teste.com")
                .password("dev")
                .profile(UserProfile.ADMIN)
                .isActive(true)
                .build();

        authority = Authority.builder()
                .id(1L)
                .name("ADMIN")
                .build();

        userRequestDTO = UserRequestDTO.builder()
                .name("Liana")
                .email("dev2@teste.com")
                .password("dev")
                .profile(UserProfile.FUNCIONARIO)
                .build();

        userResponseDTO = UserResponseDTO.builder()
                .id(1L)
                .name("Gabriella")
                .email("dev@teste.com")
                .profile(UserProfile.ADMIN)
                .isActive(true)
                .build();
    }

    @Test
    void getAllUsers_WhenCalled_ShouldReturnPageOfUsers() {
        var pageable = PageRequest.of(1, 10);
        List<User> userList = List.of(user);
        Page<User> userPage = new PageImpl<>(userList, pageable, userList.size());

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toUserResponseDTO(user)).thenReturn(userResponseDTO);

        var users = userService.getAllUsers(pageable);

        assertThat(users)
                .isNotEmpty()
                .hasSize(1);
    }

    @Test
    void getUserById_WhenIdExists_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDTO(user)).thenReturn(userResponseDTO);

        var user = userService.getUserById(1L);

        assertThat(user)
                .isNotNull()
                .extracting(UserResponseDTO::name, UserResponseDTO::email)
                .containsExactly("Gabriella", "dev@teste.com");
    }

    @Test
    void getUserById_WhenIdDoesNotExist_ShouldThrowException() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ResourceAccessException.class, () -> userService.getUserById(5L));
    }

    @Test
    void createUser_WhenCalled_ShouldCreateAndSaveUser() {
        when(userRepository.findByEmail(userRequestDTO.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("dev")).thenReturn("encodedPassword");
        when(userMapper.toUser(userRequestDTO)).thenReturn(user);
        when(authorityRepository.findByName("ADMIN")).thenReturn(Optional.of(authority));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenReturn(userResponseDTO);

        var result = userService.createUser(userRequestDTO);

        assertThat(result)
                .isNotNull()
                .extracting(UserResponseDTO::name, UserResponseDTO::email)
                .containsExactly("Gabriella", "dev@teste.com");
    }

    @Test
    void createUser_WhenEmailAlreadyExists_ShouldThrowException() {
        when(userRepository.findByEmail(userRequestDTO.email())).thenReturn(Optional.of(user));
        assertThrows(BusinessException.class, () -> userService.createUser(userRequestDTO));
    }

    @Test
    void createUser_ShouldGrantAuthorityBasedOnProfile() {
        when(userRepository.findByEmail(userRequestDTO.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("dev")).thenReturn("encodedPassword");
        when(userMapper.toUser(userRequestDTO)).thenReturn(user);

        user.setProfile(UserProfile.FUNCIONARIO);

        when(authorityRepository.findByName("FUNCIONARIO")).thenReturn(Optional.of(authority));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenReturn(userResponseDTO);

        var result = userService.createUser(userRequestDTO);

        assertThat(user.getAuthorities())
                .isNotEmpty()
                .extracting("name")
                .containsExactly("ADMIN");
    }

    @Test
    void deleteUser_WhenCalled_ShouldDeleteUserAndSave() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        user.setIsActive(false);

        userService.deleteUser(user.getId());
        verify(userRepository).findById(user.getId());
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_WhenIdDoesNotExists_ShouldThrowException() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(5L));
    }

    @Test
    void deleteUser_WhenUserIsActive_ShouldThrowException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        assertThrows(BusinessException.class, () -> userService.deleteUser(user.getId()));
    }
}
