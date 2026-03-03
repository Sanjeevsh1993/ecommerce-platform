package com.ecommerce.shared.entity;

// STRANGLER FIG - Phase: 3 - Domain: Shared
// Migrated from: No equivalent — old system had no audit fields (mock data only)
// BC References: BC-043 (all entities extend this for audit trail)

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Base JPA entity providing audit fields for ALL entities across all services.
 * Requires @EnableJpaAuditing on the application class and an AuditorAware bean.
 *
 * Fields auto-populated by Spring Data JPA Auditing:
 * - createdAt   — set once on INSERT, never updated
 * - updatedAt   — updated on every UPDATE
 * - createdBy   — username from SecurityContext on INSERT
 * - updatedBy   — username from SecurityContext on UPDATE
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
