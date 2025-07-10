package com.rainy.homebudgettracker.transaction.service;

import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.transaction.TransactionRequest;
import com.rainy.homebudgettracker.transaction.TransactionResponse;
import com.rainy.homebudgettracker.transaction.enums.BankName;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
    public List<TransactionResponse> extractTransactions(MultipartFile file, BankName bankName) throws IOException {
        File tempFile = convertMultipartToFile(file);
        if (bankName == BankName.ING) {
            return extractTransactionsFromIng(tempFile);
        } else if (bankName == BankName.REVOLUT) {
            return extractTransactionsFromRevolut(tempFile);
        } else {
            log.warn("Unsupported bank name: {}", bankName);
            throw new IllegalArgumentException("Unsupported bank name: " + bankName);
        }
    }

    private List<TransactionResponse> extractTransactionsFromIng(File tempFile) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(tempFile), Charset.forName("Windows-1250"))))
        {
            if (skipToTableInCsv(reader, 22)) {
                List<TransactionResponse> transactions = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(";");
                    if (checkIfProperRowING(values)) {
                        TransactionResponse transaction = TransactionResponse.builder()
                                .id(UUID.randomUUID())
                                .date(values[0])
                                .details(cleanDetailsING(values[3]))
                                .transactionMethod(mapToTransactionMethodING(values[6]))
                                .amount(values[8].replace(",", "."))
                                .build();
                        transactions.add(transaction);
                    }
                }
                return transactions;
            } else {
                log.warn("Failed to skip to the table in CSV (ING): {}", tempFile.getAbsolutePath());
                throw new IOException("Failed to skip to table in CSV (ING)");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while extracting data from CSV (ING)", e);
        } finally {
            if (tempFile.exists() && !tempFile.delete()) {
                log.warn("Failed to delete temporary CSV file (ING): {}", tempFile.getAbsolutePath());
            }
        }
    }

    private List<TransactionResponse> extractTransactionsFromRevolut(File tempFile) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(tempFile), StandardCharsets.UTF_8)))
        {
            if (skipToTableInCsv(reader, 1)) {
                List<TransactionResponse> transactions = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    TransactionResponse transaction = TransactionResponse.builder()
                            .id(UUID.randomUUID())
                            .date(cleanDateRevolut(values[2]))
                            .details(values[4])
                            .transactionMethod(mapToTransactionMethodRevolut(values[0]))
                            .amount(values[5])
                            .build();
                    transactions.add(transaction);
                }
                return transactions;
            } else {
                log.warn("Failed to skip to the table in CSV (Revolut): {}", tempFile.getAbsolutePath());
                throw new IOException("Failed to skip to table in CSV (Revolut)");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while extracting data from CSV (Revolut)", e);
        } finally {
            if (tempFile.exists() && !tempFile.delete()) {
                log.warn("Failed to delete temporary CSV file (Revolut): {}", tempFile.getAbsolutePath());
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

    private String cleanDetailsING(String rawDetails) {
        if (rawDetails == null || rawDetails.isEmpty()) {
            return rawDetails;
        }
        String cleaned = rawDetails.replaceAll("^\"|\"$", "");
        return cleaned.trim();
    }


    private File convertMultipartToFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "csv";
        File tempFile = File.createTempFile("temp_", "." + originalFilename);
        file.transferTo(tempFile);

        return tempFile;
    }

    private boolean skipToTableInCsv(BufferedReader reader, int rows) throws IOException {
        for (int i = 0; i < rows; i++) {
            if (reader.readLine() == null) {
                return false;
            }
        }
        return true;
    }

    private boolean checkIfProperRowING(String[] values) {
        return values.length >= 9 &&
                !Objects.equals(values[0], "") &&
                !Objects.equals(values[3], "") &&
                !Objects.equals(values[6], "") &&
                !Objects.equals(values[8], "");
    }

    private String cleanDateRevolut(String date) {
        return date.substring(0, 10);
    }

    private String mapToTransactionMethodING(String value) {
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

    private String mapToTransactionMethodRevolut(String name) {
        return switch (name) {
            case "CARD_PAYMENT" -> "DEBIT_CARD";
            default -> "OTHER";
        };
    }
}