package com.ecommerce.dto;

import com.ecommerce.entity.OrderStatus;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutResponse {
  private Long orderId;
  private BigDecimal totalAmount;
  private OrderStatus status;
  private String transactionRef;
  private String checkoutUrl;
}
