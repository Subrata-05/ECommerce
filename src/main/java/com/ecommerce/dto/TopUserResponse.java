package com.ecommerce.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopUserResponse {
  private Long userId;
  private String email;
  private String name; // Or fullName / username depending on your User entity
  private BigDecimal totalSpent;
}
