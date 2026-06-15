package com.infy.NeoBank.service;

import com.infy.NeoBank.entity.Transaction;
import com.infy.NeoBank.enums.TransactionType;
import com.infy.NeoBank.repository.TransactionRepository;
import com.infy.NeoBank.repository.UserRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatementService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public byte[] generateMonthlyStatement(Long userId) {
        LocalDateTime start = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime end = LocalDateTime.now();

        List<Transaction> transactions = transactionRepository.findByAccountUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(userId, start, end);
        String userName = userRepository.findById(userId).map(u -> u.getFullName()).orElse("User");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);

            document.open();

            // Header
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLUE);
            Paragraph title = new Paragraph("NeoBank Monthly Statement", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("Customer: " + userName));
            document.add(new Paragraph("Period: " + start.format(DateTimeFormatter.ofPattern("MMM yyyy"))));
            document.add(new Paragraph("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))));
            document.add(Chunk.NEWLINE);

            // Table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setWidths(new float[]{3f, 2f, 4f, 2f, 2f});

            String[] headers = {"Date", "Account", "Description", "Type", "Amount"};
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(Color.DARK_GRAY);
                cell.setPadding(5);
                table.addCell(cell);
            }

            for (Transaction tx : transactions) {
                table.addCell(tx.getTransactionDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                table.addCell(tx.getAccount().getAccountNumber());
                table.addCell(tx.getDescription());
                table.addCell(tx.getType().toString());
                
                PdfPCell amountCell = new PdfPCell(new Phrase(tx.getAmount().toString()));
                if (tx.getType() == TransactionType.DEBIT) {
                    amountCell.setPhrase(new Phrase("-" + tx.getAmount().toString(), FontFactory.getFont(FontFactory.HELVETICA, 12, Color.RED)));
                } else {
                    amountCell.setPhrase(new Phrase("+" + tx.getAmount().toString(), FontFactory.getFont(FontFactory.HELVETICA, 12, Color.GREEN.darker())));
                }
                table.addCell(amountCell);
            }

            document.add(table);
            document.close();

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF statement", e);
        }
    }
}
