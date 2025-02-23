package com.rainy.homebudgettracker.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImportTransactionServiceImpl implements ImportTransactionService {
    @Override
    public List<TransactionResponse> extractTransactions(MultipartFile file) throws IOException {
        // WORKING WITH CSV FILE FROM ING BANK
        try (BufferedReader reader = new BufferedReader(new FileReader(convert(file)))) {
            if (skipToTableInCsv(reader, 22)) {
                List<TransactionResponse> transactions = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(";");
                    if (checkIfProperRow(values)) {
                        TransactionResponse transaction = TransactionResponse.builder().
                                id(UUID.randomUUID()).
                                date(values[0]).
                                details(values[3]).
                                transactionMethod(mapToTransactionMethod(values[6])).
                                amount(values[8]).
                                build();
                        transactions.add(transaction);
                    }
                }
                return transactions;

            } else {
                throw new IOException("Failed to skip to table in CSV");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while extracting data from CSV", e);
        }
    }

    @Override
    public boolean importTransactions(UUID accountId, List<TransactionRequest> transactions) {
        return false;
    }

    private File convert(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("temp_", file.getOriginalFilename());
        file.transferTo(tempFile);

        return tempFile;
    }

    private boolean skipToTableInCsv(BufferedReader reader, int rows) throws IOException {
        for (int i = 0; i < rows; i++) {
            reader.readLine();
        }

        return true;
    }

    private boolean checkIfProperRow(String[] values) {
        return values.length >= 9 &&
                !Objects.equals(values[0], "") &&
                !Objects.equals(values[3], "") &&
                !Objects.equals(values[6], "") &&
                !Objects.equals(values[8], "");
    }

    private String mapToTransactionMethod(String value) {
        return switch (value) {
            case "TR.KART" -> " DEBIT_CARD";
            case "PRZELEW" -> "TRANSFER";
            case "EXPRESS" -> "EXPRESS_TRANSFER";
            case "TR.BLIK" -> "BLIK";
            case "P.BLIK" -> "BLIK_TRANSFER";
            default -> "OTHER";
        };
    }
}
