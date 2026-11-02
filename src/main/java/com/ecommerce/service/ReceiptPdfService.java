package com.ecommerce.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import org.springframework.stereotype.Service;

@Service
public class ReceiptPdfService {

  public byte[] generatePaymentReceipt(
      String orderId, String txRef, String amount, String customerName) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Document document = new Document(PageSize.A4);

    try {
      PdfWriter.getInstance(document, out);
      document.open();

      // Font configurations
      Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.DARK_GRAY);
      Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
      Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

      // Document Header
      Paragraph title = new Paragraph("CHAPA PAYMENT RECEIPT", titleFont);
      title.setAlignment(Element.ALIGN_CENTER);
      document.add(title);
      document.add(new Paragraph("\n"));

      // Meta Details Block
      document.add(new Paragraph("Order ID: " + orderId, normalFont));
      document.add(new Paragraph("Transaction Reference: " + txRef, normalFont));
      document.add(new Paragraph("Customer Name: " + customerName, normalFont));
      document.add(new Paragraph("Payment Status: PAID", boldFont));
      document.add(new Paragraph("\n"));

      // Summary Table using modern PdfPTable
      PdfPTable table = new PdfPTable(2);
      table.setWidthPercentage(100);
      table.setSpacingBefore(10f);

      // Table Headers
      PdfPCell header1 = new PdfPCell(new Paragraph("Description", boldFont));
      header1.setPadding(8);
      PdfPCell header2 = new PdfPCell(new Paragraph("Amount", boldFont));
      header2.setPadding(8);

      table.addCell(header1);
      table.addCell(header2);

      // Table Data Rows
      PdfPCell cell1 = new PdfPCell(new Paragraph("Order Settled via Chapa Gateway", normalFont));
      cell1.setPadding(8);
      PdfPCell cell2 = new PdfPCell(new Paragraph(amount + " ETB", normalFont));
      cell2.setPadding(8);

      table.addCell(cell1);
      table.addCell(cell2);

      document.add(table);
      document.close();

    } catch (DocumentException e) {
      e.printStackTrace();
    }

    return out.toByteArray();
  }
}
