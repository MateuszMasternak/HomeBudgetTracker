package com.rainy.homebudgettracker.transaction.service;

import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.TransactionRequest;
import com.rainy.homebudgettracker.transaction.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets; // Zaimportuj StandardCharsets
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class ImportTransactionServiceImpl implements ImportTransactionService {
    private final TransactionService transactionService;

    @Override
    public List<TransactionResponse> extractTransactions(MultipartFile file) throws IOException {
        // WORKING WITH CSV FILE FROM ING BANK
        File tempFile = convert(file);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tempFile), Charset.forName("Windows-1250")))) {
            if (skipToTableInCsv(reader, 22)) {
                List<TransactionResponse> transactions = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(";");
                    if (checkIfProperRow(values)) {
                        TransactionResponse transaction = TransactionResponse.builder().
                                id(UUID.randomUUID()).
                                date(values[0]).
                                details(cleanDetails(values[3])).
                                transactionMethod(mapToTransactionMethod(values[6])).
                                amount(values[8].replace(",", ".")).
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
        } finally {
            if (tempFile.exists()) {
                if (!tempFile.delete()) {
                    log.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath());
                } else {
                    log.info("Temporary file deleted successfully: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    @Override
    public boolean importTransactions(UUID accountId, List<TransactionRequest> transactions)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        for (TransactionRequest transaction : transactions) {
            transactionService.createTransactionForCurrentUser(accountId, transaction);
        }

        return true;
    }

    private String cleanDetails(String rawDetails) {
        if (rawDetails == null || rawDetails.isEmpty()) {
            return rawDetails;
        }
        String cleaned = rawDetails.replaceAll("^\"|\"$", "");
        return cleaned.trim();
    }


    private File convert(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "csv";
        File tempFile = File.createTempFile("temp_", "." + originalFilename);
        file.transferTo(tempFile);

        return tempFile;
    }

    private boolean skipToTableInCsv(BufferedReader reader, int rows) throws IOException {
        for (int i = 0; i < rows; i++) {
            if (reader.readLine() == null) {
                // Plik skończył się przedwcześnie
                return false;
            }
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
}