package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.handler.exception.WrongFileFormatException;
import com.rainy.homebudgettracker.transaction.dto.TransactionResponse;
import com.rainy.homebudgettracker.transaction.service.extractor.IngTransactionExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IngTransactionExtractorTest {

    private IngTransactionExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new IngTransactionExtractor();
    }

    @Test
    @DisplayName("Should extract transactions correctly from a valid ING file")
    void shouldExtractTransactionsFromValidFile() {
        String header = IntStream.range(0, 21).mapToObj(i -> "just a random line before the header" + i).collect(Collectors.joining("\n"));
        String csvContent = header + "\n"
                + "\"Data transakcji\";\"Data księgowania\";\"Dane kontrahenta\";\"Tytuł\";\"Nr rachunku\";\"Nazwa banku\";\"Szczegóły\";\"Nr transakcji\";\"Kwota transakcji (waluta rachunku)\";\"Waluta\";\"Kwota blokady/zwolnienie blokady\";\"Waluta\";\"Kwota płatności w walucie\";\"Waluta\";\"Konto\";\"Saldo po transakcji\";\"Waluta\";;;;\n"
                + "2025-07-10;2025-07-10;SKLEP ABC;ZAKUP PRZY UZYCIU KARTY;;ING;TR.KART;'124242';-150,55;PLN\n"
                + "2025-07-09;2025-07-09;JAN KOWALSKI;PRZELEW PRZYCHODZACY;;ING;PRZELEW;'353453';1200,00;PLN\n"
                + "2025-07-08;2025-07-08;SKLEP XYZ;ZAKUP PRZY UZYCIU BLIKA;;ING;TR.BLIK;'642233';-50,00;PLN\n"
                + "2025-07-07;2025-07-07;JAN KOWALSKI;PRZELEW WYCHODZACY EXPRESS;;ING;EXPRESS;'123456';-200,00;PLN\n"
                + "2025-07-06;2025-07-06;JAN KOWALSKI;PRZELEW WYCHODZĄCY BLIK;;ING;P.BLIK;'789012';-300,00;PLN\n"
                + "2025-07-05;2025-07-05;SKLEP XYZ;ZAKUP PRZY UZYCIU PAYPAL;;ING;PAYPAL;'456789';-100,00;PLN";


        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(Charset.forName("Windows-1250")));

        List<TransactionResponse> transactions = extractor.extract(inputStream);

        assertThat(transactions).hasSize(6);

        TransactionResponse debitCard = transactions.get(0);
        assertThat(debitCard.date()).isEqualTo("2025-07-10");
        assertThat(debitCard.details()).isEqualTo("ZAKUP PRZY UZYCIU KARTY");
        assertThat(debitCard.transactionMethod()).isEqualTo("DEBIT_CARD");
        assertThat(debitCard.amount()).isEqualTo("-150.55");

        TransactionResponse bankTransfer = transactions.get(1);
        assertThat(bankTransfer.date()).isEqualTo("2025-07-09");
        assertThat(bankTransfer.details()).isEqualTo("PRZELEW PRZYCHODZACY");
        assertThat(bankTransfer.transactionMethod()).isEqualTo("BANK_TRANSFER");
        assertThat(bankTransfer.amount()).isEqualTo("1200.00");

        TransactionResponse blik = transactions.get(2);
        assertThat(blik.date()).isEqualTo("2025-07-08");
        assertThat(blik.details()).isEqualTo("ZAKUP PRZY UZYCIU BLIKA");
        assertThat(blik.transactionMethod()).isEqualTo("BLIK");
        assertThat(blik.amount()).isEqualTo("-50.00");

        TransactionResponse expressTransfer = transactions.get(3);
        assertThat(expressTransfer.date()).isEqualTo("2025-07-07");
        assertThat(expressTransfer.details()).isEqualTo("PRZELEW WYCHODZACY EXPRESS");
        assertThat(expressTransfer.transactionMethod()).isEqualTo("EXPRESS_TRANSFER");
        assertThat(expressTransfer.amount()).isEqualTo("-200.00");

        TransactionResponse blikTransfer = transactions.get(4);
        assertThat(blikTransfer.date()).isEqualTo("2025-07-06");
        assertThat(blikTransfer.details()).isEqualTo("PRZELEW WYCHODZĄCY BLIK");
        assertThat(blikTransfer.transactionMethod()).isEqualTo("BLIK_TRANSFER");
        assertThat(blikTransfer.amount()).isEqualTo("-300.00");

        TransactionResponse other = transactions.get(5);
        assertThat(other.date()).isEqualTo("2025-07-05");
        assertThat(other.details()).isEqualTo("ZAKUP PRZY UZYCIU PAYPAL");
        assertThat(other.transactionMethod()).isEqualTo("OTHER");
        assertThat(other.amount()).isEqualTo("-100.00");
    }

    @Test
    @DisplayName("Should throw WrongFileFormatException for a file with incorrect header")
    void shouldThrowExceptionForInvalidHeader() {
        String header = IntStream.range(0, 21).mapToObj(i -> "line " + i).collect(Collectors.joining("\n"));
        String csvContent = header + "\n"
                + "Date;Amount;Currency\n"
                + "2025-07-10;-150,55;PLN";

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(Charset.forName("Windows-1250")));

        assertThatThrownBy(() -> extractor.extract(inputStream))
                .isInstanceOf(WrongFileFormatException.class)
                .hasMessageContaining("header in line 22 does not match the expected export format");
    }

    @Test
    @DisplayName("Should return an empty list if file contains only a valid header")
    void shouldReturnEmptyListForValidHeaderAndNoData() {
        String header = IntStream.range(0, 21).mapToObj(i -> "line " + i).collect(Collectors.joining("\n"));
        String csvContent = header + "\n\"Data transakcji\";\"Data księgowania\";\"Dane kontrahenta\";\"Tytuł\";\"Nr rachunku\";\"Nazwa banku\";\"Szczegóły\";\"Nr transakcji\";\"Kwota transakcji (waluta rachunku)\";\"Waluta\";\"Kwota blokady/zwolnienie blokady\";\"Waluta\";\"Kwota płatności w walucie\";\"Waluta\";\"Konto\";\"Saldo po transakcji\";\"Waluta\";;;;";

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(Charset.forName("Windows-1250")));

        List<TransactionResponse> transactions = extractor.extract(inputStream);

        assertThat(transactions).isEmpty();
    }
}