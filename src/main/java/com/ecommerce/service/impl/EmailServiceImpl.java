package com.ecommerce.service.impl;

import com.ecommerce.config.EnvConfig;
import com.ecommerce.service.EmailService;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailServiceImpl implements EmailService {

  private final EnvConfig envConfig;

  @Override
  public void sendEmailWithAttachment(
      String toEmail,
      String subject,
      String messageBody,
      byte[] attachmentBytes,
      String attachmentName) {

    validateParameters(toEmail, subject, messageBody);

    Properties properties = new Properties();
    properties.put("mail.smtp.auth", "true");
    properties.put("mail.smtp.starttls.enable", "true"); // Secure connection
    properties.put("mail.smtp.host", envConfig.getSmtpHost());
    properties.put("mail.smtp.port", envConfig.getSmtpPort());
    Session session =
        Session.getInstance(
            properties,
            new Authenticator() {
              @Override
              protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    envConfig.getSmtpUsername(), envConfig.getSmtpPassword());
              }
            });

    String htmlBody =
        """
                <div style="font-family: sans-serif; padding: 20px;">
                    <h2 style="color: #2bcb5c;">Payment Successful!</h2>
                    <p>Thank you for your purchase with BogiTech Solutions.</p>
                    <p>Your digital receipt and invoice are attached to this email.</p>
                </div>
                """;

    try {
      Message message = new MimeMessage(session);
      message.setFrom(new InternetAddress(envConfig.getSmtpUsername()));
      message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("buuzuu450@gmail.com"));
      message.setSubject(subject);
      message.setContent(htmlBody, "text/html; charset=utf-8");
      Transport.send(message);

      log.info("Email successfully dispatched to recipient: {}", toEmail);

    } catch (MessagingException e) {
      log.error("Failed to compile or dispatch email pipeline to {}: {}", toEmail, e.getMessage());
      throw new RuntimeException("Email dispatch failed: " + e.getMessage(), e);
    } catch (Exception e) {
      log.error("Unexpected error occurred while sending email to {}: {}", toEmail, e.getMessage());
      throw new RuntimeException("Unexpected mail error: " + e.getMessage(), e);
    }
  }

  private void validateParameters(String toEmail, String subject, String messageBody) {
    if (!StringUtils.hasText(toEmail)) {
      throw new IllegalArgumentException("Recipient email address cannot be null or empty.");
    }
    if (!StringUtils.hasText(subject)) {
      throw new IllegalArgumentException("Email subject cannot be null or empty.");
    }
    if (!StringUtils.hasText(messageBody)) {
      throw new IllegalArgumentException("Email message body cannot be null or empty.");
    }
  }
}
