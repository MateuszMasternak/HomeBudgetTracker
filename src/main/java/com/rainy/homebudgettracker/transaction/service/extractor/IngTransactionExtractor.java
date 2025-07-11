package com.rainy.homebudgettracker.transaction.service.extractor;

import com.rainy.homebudgettracker.handler.exception.FileProcessingException;
import com.rainy.homebudgettracker.handler.exception.WrongFileFormatException;
import com.rainy.homebudgettracker.transaction.TransactionResponse;
import com.rainy.homebudgettracker.transaction.enums.BankName;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class IngTransactionExtractor implements TransactionExtractor {
    private static final String EXPECTED_HEADER = "\"Data transakcji\";\"Data księgowania\";\"Dane kontrahenta\";\"Tytuł\";\"Nr rachunku\";\"Nazwa banku\";\"Szczegóły\";\"Nr transakcji\";\"Kwota transakcji (waluta rachunku)\";\"Waluta\";\"Kwota blokady/zwolnienie blokady\";\"Waluta\";\"Kwota płatności w walucie\";\"Waluta\";\"Konto\";\"Saldo po transakcji\";\"Waluta\";;;;";
    private static final int HEADER_ROW_NUMBER = 22;
    private static final int COL_DATE = 0;
    private static final int COL_DETAILS = 3;
    private static final int COL_TRANSACTION_METHOD = 6;
    private static final int COL_AMOUNT = 8;

    @Override
    public boolean supports(BankName bankName) {
        return BankName.ING.equals(bankName);
    }

    @Override
    public List<TransactionResponse> extract(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, Charset.forName("Windows-1250"))))
        {
            validateHeaderAndSkip(reader);

            List<TransactionResponse> transactions = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(";", -1);
                if (checkIfProperRow(values)) {
                    TransactionResponse transaction = TransactionResponse.builder()
                            .id(UUID.randomUUID())
                            .date(values[COL_DATE])
                            .details(cleanDetails(values[COL_DETAILS]))
                            .transactionMethod(mapToTransactionMethod(values[COL_TRANSACTION_METHOD]))
                            .amount(values[COL_AMOUNT].replace(",", "."))
                            .build();
                    transactions.add(transaction);
                }
            }
            return transactions;
        } catch (IOException e) {
            log.error("I/O error while reading ING file stream", e);
            throw new FileProcessingException("Could not read the Revolut file due to a system error.", e);
        }
    }

    private boolean checkIfProperRow(String[] values) {
        return values.length >= 9
                && !Objects.equals(values[COL_DATE], "")
                && !Objects.equals(values[COL_DETAILS], "")
                && !Objects.equals(values[COL_TRANSACTION_METHOD], "")
                && !Objects.equals(values[COL_AMOUNT], "");
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

    private void validateHeaderAndSkip(BufferedReader reader) throws IOException {
        for (int i = 0; i < HEADER_ROW_NUMBER - 1; i++) {
            if (reader.readLine() == null) {
                throw new WrongFileFormatException("The file is too short. The expected header was not found in the line "
                        + HEADER_ROW_NUMBER);
            }
        }

        String headerLine = reader.readLine();
        if (headerLine == null || !headerLine.trim().equals(EXPECTED_HEADER)) {
            throw new WrongFileFormatException("Incorrect file format. The header in line " + HEADER_ROW_NUMBER
                    + " does not match the expected export format from ING");
        }
    }
}
