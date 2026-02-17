package com.bikefinder.auth.infrastructure.persistence.repository;

import com.bikefinder.auth.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {

    // ✅ AGREGAR JOIN FETCH para cargar credentials
    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.credential WHERE u.email = :email")
    Optional<UserEntity> findByEmail(@Param("email") String email);

    // ✅ También para búsqueda por identidad social
    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.credential WHERE u.id IN " +
            "(SELECT si.user.id FROM SocialIdentityEntity si WHERE si.provider = :provider AND si.providerUid = :providerUid)")
    Optional<UserEntity> findBySocialIdentity(@Param("provider") String provider, @Param("providerUid") String providerUid);

    boolean existsByEmail(String email);
}
