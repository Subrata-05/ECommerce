package com.ecommerce.controller;

import com.ecommerce.dto.OrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.dto.PageResponse;
import com.ecommerce.dto.PaymentRequest;
import com.ecommerce.dto.PaymentResponse;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.entity.User;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.PaymentService;
import com.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Orders", description = "Endpoints for placing and viewing orders")
public class OrderController {

  private final OrderService orderService;
  private final PaymentService chapaPaymentService;
  private final UserService userService;

  @PostMapping
  @Operation(summary = "Checkout / Place a new order")
  public ResponseEntity<PaymentResponse> createOrder(
      @AuthenticationPrincipal UserDetails userDetails, @RequestBody OrderRequest request) {

    User userProfile = userService.findByEmail(userDetails.getUsername());

    // 1. Create the order in the database
    OrderResponse order = orderService.createOrder(userDetails.getUsername(), request);

    // 2. Initialize Chapa payment using the newly created order's ID and total
    PaymentResponse paymentResponse =
        chapaPaymentService.initializePayment(
            new PaymentRequest(
                order.getId(),
                order.getTotalAmount(),
                "ETB",
                userProfile.getEmail(),
                userProfile.getFirstName(),
                userProfile.getLastName(),
                order.getTxRef()));

    return ResponseEntity.status(HttpStatus.CREATED).body(paymentResponse);
  }

  @GetMapping("/paged")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Get Paged Orders")
  public ResponseEntity<PageResponse<OrderResponse>> getPaginatedOrders(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {

    return ResponseEntity.ok(orderService.getPaginatedOrders(page, size, sortBy, sortDir));
  }

  @GetMapping
  @Operation(summary = "Get current authenticated user's order history")
  public ResponseEntity<List<OrderResponse>> getUserOrders(
      @AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(orderService.getUserOrders(userDetails.getUsername()));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get order details by Order ID")
  public ResponseEntity<OrderResponse> getOrderById(
      @PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(orderService.getOrderById(id, userDetails.getUsername()));
  }

  @PatchMapping("/{id}/status")
  @Operation(summary = "Update order status (Admin only)")
  public ResponseEntity<OrderResponse> updateOrderStatus(
      @PathVariable Long id, @RequestParam OrderStatus status) {
    return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
  }
}
