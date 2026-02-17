package com.bikefinder.auth.infrastructure.persistence.adapter;

import com.bikefinder.auth.domain.model.User;
import com.bikefinder.auth.domain.repository.UserRepository;
import com.bikefinder.auth.domain.valueobject.Email;
import com.bikefinder.auth.domain.valueobject.UserId;
import com.bikefinder.auth.infrastructure.persistence.entity.CredentialEntity;
import com.bikefinder.auth.infrastructure.persistence.entity.UserEntity;
import com.bikefinder.auth.infrastructure.persistence.mapper.CredentialMapper;
import com.bikefinder.auth.infrastructure.persistence.mapper.UserMapperManual;
import com.bikefinder.auth.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaRepository;
    private final UserMapperManual userMapper;
    private final CredentialMapper credentialMapper;

    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findById(id.value()).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        log.info("Buscando usuario por email: {}", email.value());
        Optional<UserEntity> entityOpt = jpaRepository.findByEmail(email.value());

        if (entityOpt.isPresent()) {
            log.info("Usuario encontrado: {}", entityOpt.get().getId());
            log.info("Credential: {}", entityOpt.get().getCredential() != null ? "EXISTS" : "NULL");
        }

        return entityOpt.map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findBySocialIdentity(String provider, String providerUid) {
        return jpaRepository.findBySocialIdentity(provider, providerUid).map(userMapper::toDomain);
    }

    @Override
    @Transactional
    public User save(User user) {
        log.info("Guardando usuario: {}", user.getId().value());

        // ✅ CASO 1: Usuario NUEVO (sin ID persistente o sin credential en BD)
        if (user.getCredential() != null) {
            // Verificar si ya existe en BD
            Optional<UserEntity> existingOpt = jpaRepository.findById(user.getId().value());

            if (existingOpt.isPresent()) {
                // ✅ CASO 2: Usuario EXISTENTE, solo actualizar credential
                log.info("Usuario existe, actualizando credential");
                UserEntity existingUser = existingOpt.get();

                CredentialEntity credEntity = credentialMapper.toEntity(user.getCredential());
                credEntity.setUser(existingUser);
                credEntity.setUserId(existingUser.getId());
                existingUser.setCredential(credEntity);

                jpaRepository.save(existingUser);

                // Recargar para asegurar que todo está cargado
                UserEntity reloaded = jpaRepository.findById(existingUser.getId())
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado después de guardar"));

                return userMapper.toDomain(reloaded);
            } else {
                // ✅ CASO 1: Usuario NUEVO, guardar todo
                log.info("Usuario nuevo, guardando desde cero");
                UserEntity entity = userMapper.toEntity(user);

                CredentialEntity credEntity = credentialMapper.toEntity(user.getCredential());
                credEntity.setUser(entity);
                credEntity.setUserId(entity.getId());
                entity.setCredential(credEntity);

                jpaRepository.save(entity);

                // Recargar
                UserEntity reloaded = jpaRepository.findById(entity.getId())
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado después de guardar"));

                return userMapper.toDomain(reloaded);
            }
        } else {
            // ✅ CASO 3: Usuario sin credential (solo datos básicos)
            log.info("Guardando usuario sin credential");
            UserEntity entity = userMapper.toEntity(user);
            jpaRepository.save(entity);

            UserEntity reloaded = jpaRepository.findById(entity.getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado después de guardar"));

            return userMapper.toDomain(reloaded);
        }
    }

    @Override
    @Transactional
    public void deleteById(UserId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.value());
    }
}
