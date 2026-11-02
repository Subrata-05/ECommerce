package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  @Query(
      "SELECT p FROM Product p JOIN p.category c WHERE "
          + "(:category = 'all' OR LOWER(c.name) = LOWER(:category)) AND "
          + "(:searchTerm IS NULL OR :searchTerm = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
  Page<Product> findWithFilters(
      @Param("category") String category,
      @Param("searchTerm") String searchTerm,
      Pageable pageable);

  List<Product> findByCategoryId(Long categoryId);

  Streamable<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
      String query, String query2);

  @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity <= :threshold")
  long countOutOfStockProducts(@Param("threshold") int threshold);

  long countByStockQuantityLessThanEqual(int threshold);

  long countByStockQuantityGreaterThanAndStockQuantityLessThanEqual(int min, int max);

  @Query("SELECT COALESCE(SUM(p.stockQuantity), 0) FROM Product p")
  Long countTotalStockQuantity();
}
