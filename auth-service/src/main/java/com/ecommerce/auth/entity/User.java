package com.ecommerce.auth.entity;

// STRANGLER FIG - Phase: 4 - Domain: Authentication
// Migrated from: web.xml security-role declarations + CustomerBO.java role logic
// BC References: BC-034 (user roles), BC-035 (password hashing), BC-036 (account state)

import com.ecommerce.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", schema = "ecommerce_auth",
        uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    // BC-035: passwords must be BCrypt-hashed, never stored in plain text
    @Column(nullable = false, length = 255)
    private String passwordHash;

    // BC-034: roles map to old web.xml security-role: admin → ROLE_ADMIN, customer → ROLE_CUSTOMER
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", schema = "ecommerce_auth",
            joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false, length = 50)
    @Builder.Default
    private Set<String> roles = new HashSet<>();

    // BC-036: account enabled/locked state
    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean accountNonLocked = true;

    // BC-037: password reset token (transient — stored in separate table via RefreshToken pattern)
    @Column(length = 255)
    private String passwordResetToken;

    @Column
    private java.time.Instant passwordResetTokenExpiry;
}
