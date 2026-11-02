package com.ecommerce.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderAnalytics {
  private long totalOrders;
  private long totalPendingOrders;
  private long totalCompletedOrders;
  private long totalCanceledOrders;
}
