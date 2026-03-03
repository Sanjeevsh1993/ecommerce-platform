package com.ecommerce.order.repository;

import com.ecommerce.order.entity.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

    // BC-013: all order history for customer
    List<OrderHistory> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    // BC-020: toggle external — when false, exclude external=true records
    @Query("SELECT o FROM OrderHistory o WHERE o.customerId = :customerId " +
           "AND (:includeExternal = true OR o.external = false) " +
           "ORDER BY o.createdAt DESC")
    List<OrderHistory> findByCustomerIdWithExternalFilter(Long customerId, boolean includeExternal);
}
