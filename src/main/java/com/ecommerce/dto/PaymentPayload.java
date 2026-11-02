package com.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 👈 This fixes the crash on extra JSON fields!
public class PaymentPayload {
  private String tx_ref;
  private Object order_id; // or Long order_id depending on how you pass it
  private boolean success;

  // Optional helper method if you want to check Chapa's native "status" field directly:
  private String status;

  public boolean isSuccess() {
    return success || "success".equalsIgnoreCase(status);
  }
}
