package com.bikefinder.auth.infrastructure.security.userdetails;

import com.bikefinder.auth.domain.repository.UserRepository;
import com.bikefinder.auth.domain.valueobject.Email;
import com.bikefinder.auth.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(new Email(email))
                .map(this::mapToUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }

    public UserDetails loadUserByUserId(UUID userId) {
        return userRepository.findById(new UserId(userId))
                .map(this::mapToUserDetails)
                .orElse(null);
    }

    private UserDetails mapToUserDetails(com.bikefinder.auth.domain.model.User user) {
        // En producción, cargar roles desde DB
        return new User(
                user.getEmail().value(),
                "", // Password vacío porque JWT ya validó
                true, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                !user.isLocked(), // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
