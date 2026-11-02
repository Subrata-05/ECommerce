package com.ecommerce.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
  private List<OrderItemRequest> items;
  private String shippingAddress;
  private String paymentMethod; // e.g., "chapa", "paypal", etc.
  private String returnUrl; // URL to redirect after payment
}
