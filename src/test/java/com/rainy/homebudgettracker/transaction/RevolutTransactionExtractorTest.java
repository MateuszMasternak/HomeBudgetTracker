package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.handler.exception.WrongFileFormatException;
import com.rainy.homebudgettracker.transaction.dto.TransactionResponse;
import com.rainy.homebudgettracker.transaction.service.extractor.RevolutTransactionExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RevolutTransactionExtractorTest {

    private RevolutTransactionExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new RevolutTransactionExtractor();
    }

    @Test
    @DisplayName("Should extract transactions correctly from a valid Revolut file")
    void shouldExtractTransactionsFromValidFile() {
        String csvContent = "Type,Product,Started Date,Completed Date,Description,Amount,Fee,Currency,State,Balance\n"
                + "CARD_PAYMENT,Current,2025-07-10 12:30:00,2025-07-11 12:31:00,Sklep spozywczy,-25.50,0,PLN,COMPLETED,1000.00\n"
                + "TRANSFER,Current,2025-07-09 08:00:00,2025-07-09 08:01:00,Przelew od Anny,500.00,0,PLN,COMPLETED,1025.50\n"
                + "OTP_PAYMENT,Current,2025-07-08 10:00:00,2025-07-08 10:01:00,Inny sklep,-15.00,0,PLN,COMPLETED,525.50\n"
                + "TOPUP,Current,2025-07-08 10:00:00,2025-07-08 10:01:00,Przelew od pracodawcy,1000.00,0,PLN,COMPLETED,1525.50\n"
                + "TEST,Current,2025-07-08 10:00:00,2025-07-08 10:01:00,TEST,-20.00,0,PLN,COMPLETED,1505.50";

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        List<TransactionResponse> transactions = extractor.extract(inputStream);

        assertThat(transactions).hasSize(5);

        TransactionResponse debitCard = transactions.get(0);
        assertThat(debitCard.date()).isEqualTo("2025-07-10");
        assertThat(debitCard.details()).isEqualTo("Sklep spozywczy");
        assertThat(debitCard.transactionMethod()).isEqualTo("DEBIT_CARD");
        assertThat(debitCard.amount()).isEqualTo("-25.50");

        TransactionResponse bankTransfer = transactions.get(1);
        assertThat(bankTransfer.date()).isEqualTo("2025-07-09");
        assertThat(bankTransfer.details()).isEqualTo("Przelew od Anny");
        assertThat(bankTransfer.transactionMethod()).isEqualTo("BANK_TRANSFER");
        assertThat(bankTransfer.amount()).isEqualTo("500.00");

        TransactionResponse blik = transactions.get(2);
        assertThat(blik.date()).isEqualTo("2025-07-08");
        assertThat(blik.details()).isEqualTo("Inny sklep");
        assertThat(blik.transactionMethod()).isEqualTo("BLIK");
        assertThat(blik.amount()).isEqualTo("-15.00");

        TransactionResponse bankTransfer_2 = transactions.get(3);
        assertThat(bankTransfer_2.date()).isEqualTo("2025-07-08");
        assertThat(bankTransfer_2.details()).isEqualTo("Przelew od pracodawcy");
        assertThat(bankTransfer_2.transactionMethod()).isEqualTo("BANK_TRANSFER");
        assertThat(bankTransfer_2.amount()).isEqualTo("1000.00");

        TransactionResponse other = transactions.get(4);
        assertThat(other.date()).isEqualTo("2025-07-08");
        assertThat(other.details()).isEqualTo("TEST");
        assertThat(other.transactionMethod()).isEqualTo("OTHER");
        assertThat(other.amount()).isEqualTo("-20.00");
    }

    @Test
    @DisplayName("Should throw WrongFileFormatException for a file with an incorrect header")
    void shouldThrowExceptionForInvalidHeader() {
        String csvContent = "Date,Amount,Currency\n"
                + "2025-07-09 08:00:00,15.52,EUR";

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> extractor.extract(inputStream))
                .isInstanceOf(WrongFileFormatException.class)
                .hasMessage("Incorrect file format. The header does not match the expected Revolut format.");
    }

    @Test
    @DisplayName("Should throw WrongFileFormatException for an empty file")
    void shouldThrowExceptionForEmptyFile() {
        String csvContent = "";
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> extractor.extract(inputStream))
                .isInstanceOf(WrongFileFormatException.class)
                .hasMessage("The file is empty. Expected a header line.");
    }
}