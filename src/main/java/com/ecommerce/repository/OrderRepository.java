package com.ecommerce.repository;

import com.ecommerce.dto.TopUserResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  List<Order> findByUserId(Long userId);

  long countByStatus(OrderStatus status);

  long countByCreatedAtAfter(LocalDateTime dateTime);

  long countByStatusAndCreatedAtAfter(OrderStatus status, LocalDateTime dateTime);

  @Query(
      value = "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items",
      countQuery = "SELECT count(o) FROM Order o")
  Page<Order> findAllWithItems(Pageable pageable);

  @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status != 'CANCELLED'")
  BigDecimal getTotalRevenue();

  @Query(
      "SELECT SUM(o.totalAmount) FROM Order o WHERE o.status != 'CANCELLED' AND o.createdAt >= :dateTime")
  BigDecimal getTotalRevenueAfter(@Param("dateTime") LocalDateTime dateTime);

  @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi")
  Long sumTotalQuantitySold();

  @Query(
      "SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi JOIN oi.order o WHERE o.createdAt >= :dateTime")
  Long sumQuantitySoldAfter(@Param("dateTime") LocalDateTime dateTime);

  @Query(
      "SELECT new com.ecommerce.dto.TopUserResponse("
          + "o.user.id, o.user.email, o.user.firstName, SUM(o.totalAmount)) "
          + "FROM Order o WHERE o.status != 'CANCELLED' "
          + "GROUP BY o.user.id, o.user.email, o.user.firstName "
          + "ORDER BY SUM(o.totalAmount) DESC")
  List<TopUserResponse> findTopRevenueUsers(Pageable pageable);

  Order findByTransactionRef(String txRef);
}
