package com.ecommerce.controller;

import com.ecommerce.dto.GeneralAnalyticsResponse;
import com.ecommerce.dto.OrderAnalytics;
import com.ecommerce.dto.ProductAnalytics;
import com.ecommerce.dto.TopUserResponse;
import com.ecommerce.dto.UserAnalytics;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Endpoints for retrieving system analytics")
public class AnalyticsController { // Renamed from 'Analytics' to follow standard convention

  private final UserService userService;
  private final ProductService productService;
  private final OrderService orderService;

  @GetMapping("/general")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Get general analytics overview")
  public ResponseEntity<GeneralAnalyticsResponse> getGeneralAnalytics() {
    log.info("Fetching general system analytics...");

    // Aggregate data from your respective services
    long totalUsers = userService.getTotalUsersCount();
    long totalProducts = productService.getTotalProductsCount();
    long totalOrders = orderService.getTotalOrdersCount();
    BigDecimal totalRevenue = orderService.getTotalRevenue();

    GeneralAnalyticsResponse response =
        GeneralAnalyticsResponse.builder()
            .totalUsers(totalUsers)
            .totalProducts(totalProducts)
            .totalOrders(totalOrders)
            .totalRevenue(totalRevenue)
            .build();

    return ResponseEntity.ok(response);
  }

  @GetMapping("/products")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Get product analytics with optional date filter")
  public ResponseEntity<ProductAnalytics> getProductAnalytics(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate) {

    ProductAnalytics analytics = productService.getProductAnalyticsSummary(startDate);
    return ResponseEntity.ok(analytics);
  }

  @GetMapping("/orders")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Get order analytics with optional date filter")
  public ResponseEntity<OrderAnalytics> getOrderAnalytics(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate) {

    OrderAnalytics analytics = (OrderAnalytics) orderService.getOrderAnalyticsSummary(startDate);
    return ResponseEntity.ok(analytics);
  }

  @GetMapping("/users")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Get user analytics with optional date filter")
  public ResponseEntity<UserAnalytics> getUserAnalytics(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate) {

    UserAnalytics analytics = userService.getUserAnalyticsSummary(startDate);
    return ResponseEntity.ok(analytics);
  }

  @GetMapping("/top-user")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Get the user who generated the highest revenue")
  public ResponseEntity<TopUserResponse> getHighestRevenueUser() {
    TopUserResponse topUser = orderService.getHighestRevenueUser();

    if (topUser == null) {
      return ResponseEntity.noContent().build();
    }

    return ResponseEntity.ok(topUser);
  }
}
