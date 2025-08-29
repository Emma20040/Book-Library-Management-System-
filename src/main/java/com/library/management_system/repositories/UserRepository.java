package com.library.management_system.repositories;

import com.library.management_system.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserModel, UUID> {
    Optional<UserModel> findByUsername(String username);
    Optional<UserModel> findByEmail(String email);
    Optional<UserModel> findByVerificationToken(String token);
    Optional<UserModel> findByResetToken(String resetToken);
}

