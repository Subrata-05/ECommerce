package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class DirectChargeRequest {
  private String amount;
  private String currency;
  private String email;
  private String firstName;
  private String lastName;
}
