package com.ecommerce.auth.entity;

// STRANGLER FIG - Phase: 4 - Domain: Authentication
// Migrated from: (new pattern — legacy app had no token refresh)
// BC References: BC-038 (refresh token rotation), BC-039 (token expiry 7 days)

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens", schema = "ecommerce_auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // BC-038: token stored as opaque hash; raw token only in HTTP-only cookie
    @Column(nullable = false, unique = true, length = 512)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // BC-039: 7-day expiry (604800000ms)
    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}
