package com.ecommerce.service.impl;

import com.ecommerce.dto.OrderAnalytics;
import com.ecommerce.dto.OrderItemRequest;
import com.ecommerce.dto.OrderItemResponse;
import com.ecommerce.dto.OrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.dto.PageResponse;
import com.ecommerce.dto.TopUserResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.OrderService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

  private final OrderRepository orderRepository;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;

  @Override
  @Transactional
  public OrderResponse createOrder(String userEmail, OrderRequest request) {
    User user =
        userRepository
            .findByEmail(userEmail)
            .orElseThrow(
                () -> new ResourceNotFoundException("User not found with email: " + userEmail));

    if (request.getItems() == null || request.getItems().isEmpty()) {
      throw new BadRequestException("Cannot place an order with no items.");
    }

    Order order =
        Order.builder()
            .user(user)
            .status(OrderStatus.PENDING)
            .items(new ArrayList<>())
            .totalAmount(BigDecimal.ZERO)
            .build();

    BigDecimal totalAmount = BigDecimal.ZERO;

    for (OrderItemRequest itemReq : request.getItems()) {
      Product product =
          productRepository
              .findById(itemReq.getProductId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Product not found with id: " + itemReq.getProductId()));

      if (product.getStockQuantity() < itemReq.getQuantity()) {
        throw new BadRequestException("Insufficient stock for product: " + product.getName());
      }

      // Deduct stock
      product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
      productRepository.save(product);

      BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
      totalAmount = totalAmount.add(itemTotal);

      OrderItem orderItem =
          OrderItem.builder()
              .order(order)
              .product(product)
              .quantity(itemReq.getQuantity())
              .price(product.getPrice())
              .build();

      order.getItems().add(orderItem);
    }

    order.setTotalAmount(totalAmount);
    order.setTransactionRef(
        "tx-order-" + order.getId() + "-" + java.util.UUID.randomUUID().toString().substring(0, 8));
    Order savedOrder = orderRepository.save(order);

    return mapToResponse(savedOrder);
  }

  @Transactional(readOnly = true)
  @Override
  public List<OrderResponse> getUserOrders(String userEmail) {
    User user =
        userRepository
            .findByEmail(userEmail)
            .orElseThrow(
                () -> new ResourceNotFoundException("User not found with email: " + userEmail));
    return orderRepository.findByUserId(user.getId()).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Override
  public OrderResponse getOrderById(Long orderId, String userEmail) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Order not found with id: " + orderId));

    if (!order.getUser().getEmail().equals(userEmail)) {
      throw new ResourceNotFoundException("Unauthorized to view this order.");
    }

    return mapToResponse(order);
  }

  @Override
  @Transactional
  public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Order not found with id: " + orderId));

    order.setStatus(status);
    Order updatedOrder = orderRepository.save(order);
    return mapToResponse(updatedOrder);
  }

  private OrderResponse mapToResponse(Order order) {
    List<OrderItemResponse> itemResponses =
        order.getItems().stream()
            .map(
                item ->
                    OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
            .collect(Collectors.toList());

    return OrderResponse.builder()
        .id(order.getId())
        .userId(order.getUser().getId())
        .totalAmount(order.getTotalAmount())
        .status(order.getStatus())
        .createdAt(order.getCreatedAt())
        .txRef(order.getTransactionRef())
        .items(itemResponses)
        .build();
  }

  @Override
  public PageResponse<OrderResponse> getPaginatedOrders(
      int page, int size, String sortBy, String sortDir) {
    Sort sort =
        sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();

    Pageable pageable = PageRequest.of(page, size, sort);
    Page<Order> orderPage = orderRepository.findAllWithItems(pageable);

    List<OrderResponse> content =
        orderPage.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());

    return PageResponse.<OrderResponse>builder()
        .content(content)
        .pageNumber(orderPage.getNumber())
        .pageSize(orderPage.getSize())
        .totalElements(orderPage.getTotalElements())
        .totalPages(orderPage.getTotalPages())
        .last(orderPage.isLast())
        .build();
  }

  /**
   * Retrieves the total count of orders placed.
   *
   * @return total order count
   */
  public long getTotalOrdersCount() {
    return orderRepository.count();
  }

  /**
   * Calculates the total gross revenue across all completed/valid orders.
   *
   * @return total revenue sum
   */
  public BigDecimal getTotalRevenue() {
    BigDecimal revenue = orderRepository.getTotalRevenue();
    return revenue != null ? revenue : BigDecimal.ZERO;
  }

  /**
   * Retrieves summary analytics for orders.
   *
   * @return object containing order analytics details
   */
  public Object getOrderAnalyticsSummary() {
    return Map.of(
        "totalOrders", getTotalOrdersCount(),
        "totalRevenue", getTotalRevenue());
  }

  @Override
  public OrderAnalytics getOrderAnalyticsSummary(LocalDate startDate) {
    LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;

    long totalOrders;
    long totalPending;
    long totalCompleted;
    long totalCanceled;

    if (startDateTime != null) {
      totalOrders = orderRepository.countByCreatedAtAfter(startDateTime);
      totalPending =
          orderRepository.countByStatusAndCreatedAtAfter(OrderStatus.PENDING, startDateTime);
      totalCompleted =
          orderRepository.countByStatusAndCreatedAtAfter(OrderStatus.DELIVERED, startDateTime);
      totalCanceled =
          orderRepository.countByStatusAndCreatedAtAfter(OrderStatus.CANCELLED, startDateTime);
    } else {
      totalOrders = orderRepository.count();
      totalPending = orderRepository.countByStatus(OrderStatus.PENDING);
      totalCompleted = orderRepository.countByStatus(OrderStatus.DELIVERED);
      totalCanceled = orderRepository.countByStatus(OrderStatus.CANCELLED);
    }

    return OrderAnalytics.builder()
        .totalOrders(totalOrders)
        .totalPendingOrders(totalPending)
        .totalCompletedOrders(totalCompleted)
        .totalCanceledOrders(totalCanceled)
        .build();
  }

  @Override
  public TopUserResponse getHighestRevenueUser() {
    Pageable topOne = PageRequest.of(0, 3);
    List<TopUserResponse> topUsers = orderRepository.findTopRevenueUsers(topOne);

    log.info(topUsers.toString() + "DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDdddd");
    if (topUsers.isEmpty()) {
      return null; // Or return a default/empty response structure
    }

    return topUsers.get(0);
  }
}
