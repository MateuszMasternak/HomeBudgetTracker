package com.rainy.homebudgettracker.transaction.service.extractor;

import com.rainy.homebudgettracker.transaction.TransactionResponse;
import com.rainy.homebudgettracker.transaction.enums.BankName;
import org.springframework.stereotype.Controller;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class RevolutTransactionExtractor implements TransactionExtractor {
    @Override
    public boolean supports(BankName bankName) {
        return BankName.REVOLUT.equals(bankName);
    }

    @Override
    public List<TransactionResponse> extract(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
        {
            skipToTableInCsv(reader, 1);

            List<TransactionResponse> transactions = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                TransactionResponse transaction = TransactionResponse.builder()
                        .id(UUID.randomUUID())
                        .date(cleanDate(values[2]))
                        .details(values[4])
                        .transactionMethod(mapToTransactionMethod(values[0]))
                        .amount(values[5])
                        .build();
                transactions.add(transaction);
            }
            return transactions;
        } catch (IOException e) {
            throw new RuntimeException("Error while extracting data from CSV (Revolut)", e);
        }
    }

    private String cleanDate(String date) {
        return date.substring(0, 10);
    }

    private String mapToTransactionMethod(String name) {
        return switch (name) {
            case "CARD_PAYMENT" -> "DEBIT_CARD";
            default -> "OTHER";
        };
    }

    private void skipToTableInCsv(BufferedReader reader, int rows) throws IOException {
        for (int i = 0; i < rows; i++) {
            if (reader.readLine() == null) {
                throw new IOException("Failed to skip to table in CSV (REVOLUT), unexpected end of file.");
            }
        }
    }
}
