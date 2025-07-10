package com.rainy.homebudgettracker.transaction.service.extractor;

import com.rainy.homebudgettracker.transaction.TransactionResponse;
import com.rainy.homebudgettracker.transaction.enums.BankName;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class IngTransactionExtractor implements TransactionExtractor {

    @Override
    public boolean supports(BankName bankName) {
        return BankName.ING.equals(bankName);
    }

    @Override
    public List<TransactionResponse> extract(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, Charset.forName("Windows-1250"))))
        {
            skipToTableInCsv(reader, 22);

            List<TransactionResponse> transactions = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(";");
                if (checkIfProperRow(values)) {
                    TransactionResponse transaction = TransactionResponse.builder()
                            .id(UUID.randomUUID())
                            .date(values[0])
                            .details(cleanDetails(values[3]))
                            .transactionMethod(mapToTransactionMethod(values[6]))
                            .amount(values[8].replace(",", "."))
                            .build();
                    transactions.add(transaction);
                }
            }
            return transactions;
        } catch (IOException e) {
            throw new RuntimeException("Error while extracting data from CSV (ING)", e);
        }
    }

    private boolean checkIfProperRow(String[] values) {
        return values.length >= 9
                && !Objects.equals(values[0], "")
                && !Objects.equals(values[3], "")
                && !Objects.equals(values[6], "")
                && !Objects.equals(values[8], "");
    }

    private String cleanDetails(String rawDetails) {
        if (rawDetails == null || rawDetails.isEmpty()) return rawDetails;
        return rawDetails.replaceAll("^\"|\"$", "").trim();
    }

    private String mapToTransactionMethod(String value) {
        String name = value.replace(" ", "").replace("\"", "");
        return switch (name) {
            case "TR.KART" -> "DEBIT_CARD";
            case "PRZELEW" -> "BANK_TRANSFER";
            case "EXPRESS" -> "EXPRESS_TRANSFER";
            case "TR.BLIK" -> "BLIK";
            case "P.BLIK" -> "BLIK_TRANSFER";
            default -> "OTHER";
        };
    }

    private void skipToTableInCsv(BufferedReader reader, int rows) throws IOException {
        for (int i = 0; i < rows; i++) {
            if (reader.readLine() == null) {
                throw new IOException("Failed to skip to table in CSV (ING), unexpected end of file.");
            }
        }
    }
}
