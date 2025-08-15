package com.rainy.homebudgettracker.transaction.service.extractor;

import com.rainy.homebudgettracker.handler.exception.FileProcessingException;
import com.rainy.homebudgettracker.handler.exception.WrongFileFormatException;
import com.rainy.homebudgettracker.transaction.dto.TransactionResponse;
import com.rainy.homebudgettracker.transaction.enums.BankName;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Log4j2
public class RevolutTransactionExtractor implements TransactionExtractor {
    private static final String EXPECTED_HEADER = "Type,Product,Started Date,Completed Date,Description,Amount,Fee,Currency,State,Balance";
    private static final int COL_TYPE = 0;
    private static final int COL_STARTED_DATE = 2;
    private static final int COL_DESCRIPTION = 4;
    private static final int COL_AMOUNT = 5;

    @Override
    public boolean supports(BankName bankName) {
        return BankName.REVOLUT.equals(bankName);
    }

    @Override
    public List<TransactionResponse> extract(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
        {
            validateHeaderAndSkip(reader);

            List<TransactionResponse> transactions = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                TransactionResponse transaction = TransactionResponse.builder()
                        .id(UUID.randomUUID())
                        .date(cleanDate(values[COL_STARTED_DATE]))
                        .details(values[COL_DESCRIPTION])
                        .transactionMethod(mapToTransactionMethod(values[COL_TYPE]))
                        .amount(values[COL_AMOUNT])
                        .build();
                transactions.add(transaction);
            }
            return transactions;
        } catch (IOException e) {
            log.error("I/O error while reading Revolut file stream", e);
            throw new FileProcessingException("Could not read the Revolut file due to a system error.", e);
        }
    }

    private String cleanDate(String date) {
        return date.substring(0, 10);
    }

    private String mapToTransactionMethod(String name) {
        return switch (name) {
            case "CARD_PAYMENT" -> "DEBIT_CARD";
            case "TRANSFER", "TOPUP" -> "BANK_TRANSFER";
            case "OTP_PAYMENT" -> "BLIK";
            default -> "OTHER";
        };
    }

    private void validateHeaderAndSkip(BufferedReader reader) throws IOException {
        String headerLine = reader.readLine();
        if (headerLine == null) {
            throw new WrongFileFormatException("The file is empty. Expected a header line.");
        }
        if (!EXPECTED_HEADER.equals(headerLine.trim())) {
            throw new WrongFileFormatException("Incorrect file format. The header does not match the expected Revolut format.");
        }
    }
}
