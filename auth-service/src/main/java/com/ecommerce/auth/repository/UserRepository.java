package com.ecommerce.auth.repository;

// STRANGLER FIG - Phase: 4 - Domain: Authentication
// Migrated from: CustomerBO.java static mock lookup
// BC References: BC-034 (user lookup by email for login), BC-037 (password reset lookup)

import com.ecommerce.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // BC-034: primary auth lookup
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // BC-037: password reset flow
    Optional<User> findByPasswordResetToken(String token);
}
