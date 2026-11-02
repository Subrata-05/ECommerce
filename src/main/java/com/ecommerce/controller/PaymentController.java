package com.ecommerce.controller;

import com.ecommerce.config.EnvConfig;
import com.ecommerce.dto.DirectChargeRequest;
import com.ecommerce.dto.PaymentPayload;
import com.ecommerce.dto.PaymentRequest;
import com.ecommerce.dto.PaymentResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.service.EmailService;
import com.ecommerce.service.PaymentService;
import com.ecommerce.service.ReceiptPdfService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@Slf4j
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Endpoints for Stripe and Chapa payment processing")
public class PaymentController {

  private final PaymentService stripePaymentService;
  private final PaymentService chapaPaymentService;
  private final OrderRepository orderRepository;
  private final EnvConfig envConfig;
  private final RestClient restClient;
  private final EmailService emailService;
  private final ReceiptPdfService receiptPdfService;

  @PostMapping("/stripe/initialize")
  @SecurityRequirement(name = "bearerAuth")
  @Operation(summary = "Initialize Stripe Checkout session")
  public ResponseEntity<PaymentResponse> initStripe(@RequestBody PaymentRequest request) {
    return ResponseEntity.ok(stripePaymentService.initializePayment(request));
  }

  @PostMapping("/chapa/initialize")
  @SecurityRequirement(name = "bearerAuth")
  @Operation(summary = "Initialize Chapa payment transaction")
  public ResponseEntity<PaymentResponse> initChapa(@RequestBody PaymentRequest request) {
    return ResponseEntity.ok(chapaPaymentService.initializePayment(request));
  }

  @GetMapping("/verify")
  @Operation(summary = "Verify transaction status by provider")
  public ResponseEntity<Boolean> verifyPayment(@RequestParam String txRef) {
    return ResponseEntity.ok(chapaPaymentService.verifyPayment(txRef));
  }

  @PostMapping("/direct-charge")
  public ResponseEntity<?> processDirectCharge(@RequestBody DirectChargeRequest request) {

    Map<String, Object> body = new HashMap<>();
    body.put("amount", request.getAmount());
    body.put("currency", request.getCurrency());
    body.put("email", request.getEmail());
    body.put("first_name", request.getFirstName());
    body.put("last_name", request.getLastName());
    body.put("tx_ref", "tx-" + UUID.randomUUID().toString());

    try {
      Map<?, ?> response =
          restClient
              .post()
              .uri("https://api.chapa.co/v1/transaction/initialize")
              .header("Authorization", "Bearer " + envConfig.getChapaSecretKey())
              .body(body)
              .retrieve()
              .body(Map.class);

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PostMapping("/webhook")
  public ResponseEntity<String> handleChapaWebhook(
      @RequestHeader(value = "x-chapa-signature", required = false) String signature,
      @RequestBody String rawBody) { // 👈 Accept raw String first to preserve exact HMAC hash bytes

    // 1. Core Security: Validate the incoming Chapa HMAC-SHA256 signature
    if (signature == null || signature.isEmpty()) {
      log.warn("Unauthorized webhook attempt: Missing x-chapa-signature header.");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing signature header");
    }

    String webhookSecret = envConfig.getChapaWebhookSecret();
    try {
      // Validate signature against the exact raw request body string
      if (!WebhookVerifier.isSignatureValid(rawBody, signature, webhookSecret)) {
        log.error("Security mismatch! Invalid webhook signature detected.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature hash");
      }
    } catch (Exception e) {
      log.error("Failed to execute signature validation fallback check: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Signature parsing failure");
    }

    // 2. Deserialize raw body into PaymentPayload only after validation succeeds
    PaymentPayload payload;
    try {
      ObjectMapper mapper = new ObjectMapper();
      payload = mapper.readValue(rawBody, PaymentPayload.class);
    } catch (Exception e) {
      log.error("Failed to parse webhook payload JSON: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON structure");
    }

    // 3. Extract verified parameters safely
    String txRef = payload.getTx_ref();
    boolean isSuccess = payload.isSuccess();

    // 4. Process payment status updates and notifications
    if (isSuccess) {
      log.info("Payment successfully processed for transaction reference: {}", txRef);

      Order order = orderRepository.findByTransactionRef(txRef);

      if (order != null) {
        // Set order state to paid
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        // Fetch dynamic real values out of your database
        String customerEmail = order.getUser().getEmail();
        String customerFullName =
            order.getUser().getFirstName() + " " + order.getUser().getLastName();
        String finalAmount = order.getTotalAmount().toString();

        log.info("Compiling and sending PDF attachment confirmation to {}", customerEmail);

        // Generate receipt in-memory
        byte[] pdfBytes =
            receiptPdfService.generatePaymentReceipt(
                order.getId().toString(), txRef, finalAmount, customerFullName);

        // Dispatch email layout safely
        String emailHtmlBody =
            "<h3>Dear "
                + customerFullName
                + ",</h3>"
                + "<p>Thank you for your purchase! Your payment has been successfully processed.</p>"
                + "<p>Please see your attached payment receipt for transaction record: <b>"
                + txRef
                + "</b></p>";

        emailService.sendEmailWithAttachment(
            customerEmail,
            "Payment Confirmation - Order #" + order.getId(),
            emailHtmlBody,
            pdfBytes,
            "Receipt_" + txRef + ".pdf");
      }
    } else {
      log.warn("Payment event notification failed state reported for reference: {}", txRef);
    }

    // Always return a 200 OK quickly so Chapa stops retrying
    return ResponseEntity.ok("Webhook received successfully");
  }
}
