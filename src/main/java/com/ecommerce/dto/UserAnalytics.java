package com.ecommerce.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAnalytics {
  private long totalUsers;
  private long activeUsers;
  private long suspendedUsers;
  private long adminUsers;
}
