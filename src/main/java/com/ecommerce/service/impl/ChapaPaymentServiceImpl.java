package com.ecommerce.service.impl;

import com.ecommerce.config.EnvConfig;
import com.ecommerce.dto.PaymentRequest;
import com.ecommerce.dto.PaymentResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.service.PaymentService;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Data
@Service("chapaPaymentService")
public class ChapaPaymentServiceImpl implements PaymentService {

  // public EnvConfig envConfig;
  private final EnvConfig envConfig;

  private final RestClient restClient;

  // Single constructor for Spring dependency injection
  public ChapaPaymentServiceImpl(EnvConfig envConfig) {
    this.envConfig = envConfig;
    this.restClient = RestClient.create();
  }

  @Override
  public PaymentResponse initializePayment(PaymentRequest request) {
    Map<String, Object> body = new HashMap<>();
    body.put("amount", request.getAmount().toString());
    body.put("currency", request.getCurrency() != null ? request.getCurrency() : "ETB");
    body.put("email", request.getEmail());
    body.put("first_name", request.getFirstName());
    body.put("last_name", request.getLastName());
    body.put("tx_ref", request.getTxRef());

    try {
      Map<?, ?> response =
          restClient
              .post()
              .uri("https://api.chapa.co/v1/transaction/initialize")
              .header("Authorization", "Bearer " + envConfig.getChapaSecretKey())
              .contentType(MediaType.APPLICATION_JSON)
              .body(body)
              .retrieve()
              .body(Map.class);

      if (response != null && "success".equals(response.get("status"))) {
        Map<?, ?> data = (Map<?, ?>) response.get("data");
        String checkoutUrl = (String) data.get("checkout_url");

        return PaymentResponse.builder()
            .transactionRef(request.getTxRef())
            .checkoutUrl(checkoutUrl)
            .status("PENDING")
            .build();
      } else {
        throw new BadRequestException("Chapa initialization failed.");
      }
    } catch (Exception e) {
      throw new BadRequestException("Chapa payment error: " + e.getMessage());
    }
  }

  @Override
  public boolean verifyPayment(String transactionRef) {
    try {
      log.info(
          "Verifying transaction reference: {} using base URL: {}",
          transactionRef,
          envConfig.getBaseUrl());

      ResponseEntity<Map<String, Object>> response =
          restClient
              .get()
              .uri(envConfig.getBaseUrl() + "/transaction/verify/" + transactionRef)
              .header("Authorization", "Bearer " + envConfig.getChapaSecretKey())
              .retrieve()
              .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

      log.info("Chapa verification status code: {}", response.getStatusCode());
      log.info("Chapa verification body: {}", response.getBody());

      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        String status = (String) response.getBody().get("status");
        return "success".equalsIgnoreCase(status);
      }
    } catch (Exception e) {
      log.error("Failed to verify payment for ref {}: {}", transactionRef, e.getMessage(), e);
    }
    return false;
  }
}
