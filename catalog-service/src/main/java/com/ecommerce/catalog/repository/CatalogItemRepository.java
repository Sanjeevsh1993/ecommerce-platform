package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.CatalogItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CatalogItemRepository extends JpaRepository<CatalogItem, Long> {

    Optional<CatalogItem> findByItemCode(String itemCode);
    boolean existsByItemCode(String itemCode);

    // BC-031: search/filter active items
    @Query("SELECT c FROM CatalogItem c WHERE c.active = true AND " +
           "(:query IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(c.itemCode) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<CatalogItem> searchActive(String query, Pageable pageable);
}
