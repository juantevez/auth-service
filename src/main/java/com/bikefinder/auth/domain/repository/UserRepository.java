package com.bikefinder.auth.domain.repository;

import com.bikefinder.auth.domain.model.User;
import com.bikefinder.auth.domain.valueobject.Email;
import com.bikefinder.auth.domain.valueobject.UserId;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(UserId id);
    Optional<User> findByEmail(Email email);
    // Buscar usuario por identidad social (Proveedor + UID)
    Optional<User> findBySocialIdentity(String provider, String providerUid);

    User save(User user);
    void deleteById(UserId id);

    boolean existsByEmail(Email email);
}

