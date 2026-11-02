package com.ecommerce.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GeneralAnalyticsResponse {
  private long totalUsers;
  private long totalProducts;
  private long totalOrders;
  private BigDecimal totalRevenue;
}
