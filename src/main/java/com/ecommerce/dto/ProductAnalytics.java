package com.ecommerce.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductAnalytics {
  private long countLowInStock;
  private long totalItemInStock;
  private long countOutOfStock;
  private long totalSold;
}
