package com.ecommerce.service.impl;

import com.ecommerce.dto.PageResponse;
import com.ecommerce.dto.ProductAnalytics;
import com.ecommerce.dto.ProductRequest;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.FileUploadService;
import com.ecommerce.service.ProductService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final FileUploadService uploadService;
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final OrderRepository orderRepository;

  public PageResponse<ProductResponse> getPaginatedProducts(
      int page, int size, String sortBy, String sortDir, String cat, String searchTerm) {
    Sort sort =
        sortDir.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
    Pageable pageable = PageRequest.of(page, size, sort);

    // Normalize category parameter
    String categoryFilter = (cat == null || cat.isBlank()) ? "all" : cat.trim();
    String searchFilter = (searchTerm == null) ? "" : searchTerm.trim();

    Page<Product> productPage =
        productRepository.findWithFilters(categoryFilter, searchFilter, pageable);

    List<ProductResponse> content =
        productPage.getContent().stream()
            .map(this::mapToResponse) // Assumes you have a mapper method
            .toList();

    return PageResponse.<ProductResponse>builder()
        .content(content)
        .pageNumber(productPage.getNumber())
        .pageSize(productPage.getSize())
        .totalElements(productPage.getTotalElements())
        .totalPages(productPage.getTotalPages())
        .last(productPage.isLast())
        .build();
  }

  @Override
  public ProductResponse createProduct(ProductRequest request) {
    Category category =
        categoryRepository
            .findById(request.getCategoryId())
            .orElseThrow(
                () ->
                    new RuntimeException("Category not found with id: " + request.getCategoryId()));

    Product product =
        Product.builder()
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .stockQuantity(request.getStockQuantity())
            .category(category)
            .build();

    if (request.getImage() != null && !request.getImage().isEmpty()) {

      final String fileName = uploadService.storeFile(request.getImage());

      String fileDownloadUri =
          ServletUriComponentsBuilder.fromCurrentContextPath()
              .path("/uploads/")
              .path(fileName)
              .toUriString();

      product.setImageUrl(fileDownloadUri);
    }
    Product savedProduct = productRepository.save(product);
    return mapToResponse(savedProduct);
  }

  @Override
  public List<ProductResponse> getAllProducts() {
    return productRepository.findAll().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Override
  public ProductResponse getProductById(Long id) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    return mapToResponse(product);
  }

  @Override
  public List<ProductResponse> getProductsByCategory(Long categoryId) {
    if (!categoryRepository.existsById(categoryId)) {
      throw new RuntimeException("Category not found with id: " + categoryId);
    }
    return productRepository.findByCategoryId(categoryId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Override
  public ProductResponse updateProduct(Long id, ProductRequest request) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

    Category category =
        categoryRepository
            .findById(request.getCategoryId())
            .orElseThrow(
                () ->
                    new RuntimeException("Category not found with id: " + request.getCategoryId()));

    product.setName(request.getName());
    product.setDescription(request.getDescription());
    product.setPrice(request.getPrice());
    product.setStockQuantity(request.getStockQuantity());
    product.setCategory(category);

    if (request.getImage() != null && !request.getImage().isEmpty()) {

      final String fileName = uploadService.storeFile(request.getImage());

      String fileDownloadUri =
          ServletUriComponentsBuilder.fromCurrentContextPath()
              .path("/uploads/")
              .path(fileName)
              .toUriString();

      product.setImageUrl(fileDownloadUri);
    }

    Product updatedProduct = productRepository.save(product);
    return mapToResponse(updatedProduct);
  }

  @Override
  public void deleteProduct(Long id) {
    if (!productRepository.existsById(id)) {
      throw new ResourceNotFoundException("Product not found with id: " + id);
    }
    productRepository.deleteById(id);
  }

  @Override
  public @Nullable Object searchProducts(String query) {
    return productRepository
        .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Override
  public @Nullable Object getPopularProducts() {
    // Placeholder for popular products logic. This could be based on sales, views,
    // etc.
    // For now, we will return the first 5 products as a placeholder.
    return productRepository.findAll(PageRequest.of(0, 5)).getContent().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Override
  public @Nullable Object getProductsForYou() {
    // Placeholder for personalized product recommendations. This could be based on
    // user behavior,
    // preferences, etc.
    // For now, we will return the first 5 products as a placeholder.
    return productRepository.findAll(PageRequest.of(0, 5)).getContent().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Retrieves the total count of products in the catalog.
   *
   * @return total product count
   */
  public long getTotalProductsCount() {
    return productRepository.count();
  }

  /**
   * Retrieves summary analytics for products.
   *
   * @return object containing product analytics details
   */
  public ProductAnalytics getProductAnalyticsSummary(LocalDate startDate) {
    LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;

    // Inventory counts (current stock levels are generally global, but can be filtered if products
    // have creation dates)
    // Safely unbox wrapper Long to primitive long
    Long totalStockResult = productRepository.countTotalStockQuantity();
    long totalItemInStock = (totalStockResult != null) ? totalStockResult : 0L;

    long countOutOfStock = productRepository.countByStockQuantityLessThanEqual(0);
    long countLowInStock =
        productRepository.countByStockQuantityGreaterThanAndStockQuantityLessThanEqual(0, 5);

    Long totalSoldResult =
        (startDateTime != null)
            ? orderRepository.sumQuantitySoldAfter(startDateTime)
            : orderRepository.sumTotalQuantitySold();

    long totalSold = (totalSoldResult != null) ? totalSoldResult : 0L;

    return ProductAnalytics.builder()
        .countLowInStock(countLowInStock)
        .totalItemInStock(totalItemInStock)
        .countOutOfStock(countOutOfStock)
        .totalSold(totalSold)
        .build();
  }

  private ProductResponse mapToResponse(Product product) {
    return ProductResponse.builder()
        .id(product.getId())
        .name(product.getName())
        .description(product.getDescription())
        .price(product.getPrice())
        .stockQuantity(product.getStockQuantity())
        .imageUrl(product.getImageUrl())
        .categoryId(product.getCategory().getId())
        .categoryName(product.getCategory().getName())
        .build();
  }
}
