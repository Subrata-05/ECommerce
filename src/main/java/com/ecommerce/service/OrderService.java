package com.ecommerce.service;

import com.ecommerce.dto.OrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.dto.PageResponse;
import com.ecommerce.dto.TopUserResponse;
import com.ecommerce.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.jspecify.annotations.Nullable;

public interface OrderService {
  OrderResponse createOrder(String userEmail, OrderRequest request);

  PageResponse<OrderResponse> getPaginatedOrders(int page, int size, String sortBy, String sortDir);

  List<OrderResponse> getUserOrders(String userEmail);

  OrderResponse getOrderById(Long orderId, String userEmail);

  OrderResponse updateOrderStatus(Long orderId, OrderStatus status);

  @Nullable Object getOrderAnalyticsSummary(LocalDate startDate);

  long getTotalOrdersCount();

  BigDecimal getTotalRevenue();

  TopUserResponse getHighestRevenueUser();
}
