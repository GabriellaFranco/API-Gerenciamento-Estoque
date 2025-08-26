package com.enterprise.gestaoestoque.configuration.security;

import com.enterprise.gestaoestoque.exception.BusinessException;
import com.enterprise.gestaoestoque.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(username).orElseThrow(
                () -> new ResourceAccessException("Usuário não encontrado:  " + username));
        if (!user.getIsActive()) {
            throw new BusinessException("Usuário desativado, contate o administrador");
        }

        List<SimpleGrantedAuthority> authorities = user.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getName())).toList();
        return new User(user.getEmail(), user.getPassword(), authorities);
    }
}
