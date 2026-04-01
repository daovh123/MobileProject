package com.mobileproject.mobileprojectbackend.auth;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AuthUserRepository extends MongoRepository<AuthUser, String> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<AuthUser> findByUsername(String username);

    Optional<AuthUser> findByEmail(String email);

    long deleteByUsername(String username);
}
