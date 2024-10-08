package com.example.InvoiceGenerator.service;

import com.example.InvoiceGenerator.model.Payment;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class InvoiceGeneratorService {

    private static final String PDF_DIRECTORY = "invoices/";

    public void generateInvoice(Payment payment) {
        // Ensure the directory exists
        try {
            Path path = Paths.get(PDF_DIRECTORY);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Generate PDF file name using invoice number
        String pdfFileName = PDF_DIRECTORY + "invoice_" + payment.getInvoicenumber() + ".pdf";

        // Create the PDF document
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(pdfFileName));
            document.open();

            // Add title
            document.add(new Paragraph("Invoice", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20)));
            document.add(new Paragraph(" ")); // Add a blank line for spacing

            // 1. Receiver and User Info Table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            PdfPCell receiverCell = new PdfPCell(new Paragraph("Receiver Info: \n" + payment.getReceiverInfo()+"\nReceiver A/C: "+payment.getTargetBankAccount()));
            PdfPCell userCell = new PdfPCell(new Paragraph("Buyer Info: \n" + payment.getBuyerInfo()+"\nBuyer A/C: "+payment.getSourceBankAccount()));
            receiverCell.setPadding(10);
            userCell.setPadding(10);
            infoTable.addCell(receiverCell);
            infoTable.addCell(userCell);
            document.add(infoTable);

            document.add(new Paragraph(" ")); // Add a blank line for spacing

            // 2. Purchase Order Number (P.O. Number)
            document.add(new Paragraph("P.O. Number: " + payment.getPonumber(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            document.add(new Paragraph(" ")); // Add a blank line for spacing

            // 3. Items Table (Item Name, Quantity, Unit Price, Total)
            PdfPTable itemTable = new PdfPTable(4);
            itemTable.setWidthPercentage(100);
            itemTable.addCell("Item Name");
            itemTable.addCell("Quantity");
            itemTable.addCell("Unit Price");
            itemTable.addCell("Total");

            double totalAmount = 0; // To calculate total amount before TDS deduction
            for (Payment.Item item : payment.getItems()) {
                double itemTotal = item.getAmount() * item.getQuantity();
                totalAmount += itemTotal;

                itemTable.addCell(item.getItemName());
                itemTable.addCell(String.valueOf(item.getQuantity()));
                itemTable.addCell(String.valueOf(item.getAmount()));
                itemTable.addCell(String.valueOf(itemTotal));
            }

            PdfPCell emptyCell1 = new PdfPCell();
            PdfPCell emptyCell2 = new PdfPCell();
            emptyCell1.setBorder(Rectangle.NO_BORDER); // Remove borders for empty cells
            emptyCell2.setBorder(Rectangle.NO_BORDER); // Remove borders for empty cells
            itemTable.addCell(emptyCell1); // Empty cell under Item Name
            itemTable.addCell(emptyCell2); // Empty cell under Quantity
            itemTable.addCell(new PdfPCell(new Paragraph("Total"))); // "Total" in Unit Price column
            itemTable.addCell(new PdfPCell(new Paragraph(String.valueOf(totalAmount)))); // Display total amount in the Total column

            document.add(itemTable);

            // 4. TDS Deduction and Final Total
            double tdsAmount = (totalAmount * payment.getTds()) / 100; // Calculate TDS amount
            double finalAmount = totalAmount - tdsAmount; // Final amount after TDS deduction

            // Add TDS row
            PdfPTable tdsTable = new PdfPTable(4);
            tdsTable.setWidthPercentage(100);
            PdfPCell emptyC1 = new PdfPCell();
            PdfPCell emptyC2 = new PdfPCell();
            emptyC1.setBorder(Rectangle.NO_BORDER); // Remove borders for empty cells
            emptyC2.setBorder(Rectangle.NO_BORDER); // Remove borders for empty cells

            tdsTable.addCell(emptyC1); // Empty cell under Item Name
            tdsTable.addCell(emptyC2); // Empty cell under Quantity
            tdsTable.addCell("TDS (" + payment.getTds() + "%)"); // "TDS" in Unit Price column with TDS percentage
            tdsTable.addCell(String.valueOf(tdsAmount)); // TDS value

            // Add Final Total row
            tdsTable.addCell(emptyC1); // Empty cell under Item Name
            tdsTable.addCell(emptyC2); // Empty cell under Quantity
            tdsTable.addCell("Final Total"); // "Final Total" in Unit Price column
            tdsTable.addCell(String.valueOf(finalAmount)); // Final amount value

            document.add(tdsTable);

            document.close();
            System.out.println("Invoice PDF generated and saved as: " + pdfFileName);
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
    }
}
