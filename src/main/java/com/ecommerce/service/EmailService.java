package com.ecommerce.service;

public interface EmailService {
  void sendEmailWithAttachment(
      String toEmail,
      String subject,
      String messageBody,
      byte[] attachmentBytes,
      String attachmentName);
}
