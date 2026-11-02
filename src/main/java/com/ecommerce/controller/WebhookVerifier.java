package com.ecommerce.controller;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

@Slf4j
public class WebhookVerifier {

  /**
   * Verifies the authenticity of the Chapa webhook signature.
   *
   * @param rawJsonBody The raw JSON payload received in the request body.
   * @param chapaSignatureHeader The signature string provided in the 'x-chapa-signature' header.
   * @param webhookSecret Your secret webhook hash key configured in Chapa's dashboard.
   * @return true if the calculated signature matches Chapa's header signature.
   */
  public static boolean isSignatureValid(
      String rawJsonBody, String chapaSignatureHeader, String webhookSecret) {
    log.info(
        "Verifying Chapa webhook signature... RAWJSON: {}, HEADER: {}, SECRET: {}",
        rawJsonBody,
        chapaSignatureHeader,
        webhookSecret);
    if (chapaSignatureHeader == null || webhookSecret == null || rawJsonBody == null) {
      log.warn("One or more required parameters for signature verification are null.");
      return false;
    }

    try {
      // 1. Initialize HMAC-SHA256 using your secret key
      Mac hmacSha256 = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKey =
          new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
      hmacSha256.init(secretKey);

      // 2. Hash the raw incoming request payload body
      byte[] hashBytes = hmacSha256.doFinal(rawJsonBody.getBytes(StandardCharsets.UTF_8));
      String calculatedSignature = Hex.encodeHexString(hashBytes);

      // 3. Perform a case-insensitive match against the header
      return calculatedSignature.equalsIgnoreCase(chapaSignatureHeader);

    } catch (Exception e) {
      return false;
    }
  }
}
