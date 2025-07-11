package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.handler.exception.WrongFileFormatException;
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
        assertThat(debitCard.getDate()).isEqualTo("2025-07-10");
        assertThat(debitCard.getDetails()).isEqualTo("ZAKUP PRZY UZYCIU KARTY");
        assertThat(debitCard.getTransactionMethod()).isEqualTo("DEBIT_CARD");
        assertThat(debitCard.getAmount()).isEqualTo("-150.55");

        TransactionResponse bankTransfer = transactions.get(1);
        assertThat(bankTransfer.getDate()).isEqualTo("2025-07-09");
        assertThat(bankTransfer.getDetails()).isEqualTo("PRZELEW PRZYCHODZACY");
        assertThat(bankTransfer.getTransactionMethod()).isEqualTo("BANK_TRANSFER");
        assertThat(bankTransfer.getAmount()).isEqualTo("1200.00");

        TransactionResponse blik = transactions.get(2);
        assertThat(blik.getDate()).isEqualTo("2025-07-08");
        assertThat(blik.getDetails()).isEqualTo("ZAKUP PRZY UZYCIU BLIKA");
        assertThat(blik.getTransactionMethod()).isEqualTo("BLIK");
        assertThat(blik.getAmount()).isEqualTo("-50.00");

        TransactionResponse expressTransfer = transactions.get(3);
        assertThat(expressTransfer.getDate()).isEqualTo("2025-07-07");
        assertThat(expressTransfer.getDetails()).isEqualTo("PRZELEW WYCHODZACY EXPRESS");
        assertThat(expressTransfer.getTransactionMethod()).isEqualTo("EXPRESS_TRANSFER");
        assertThat(expressTransfer.getAmount()).isEqualTo("-200.00");

        TransactionResponse blikTransfer = transactions.get(4);
        assertThat(blikTransfer.getDate()).isEqualTo("2025-07-06");
        assertThat(blikTransfer.getDetails()).isEqualTo("PRZELEW WYCHODZĄCY BLIK");
        assertThat(blikTransfer.getTransactionMethod()).isEqualTo("BLIK_TRANSFER");
        assertThat(blikTransfer.getAmount()).isEqualTo("-300.00");

        TransactionResponse other = transactions.get(5);
        assertThat(other.getDate()).isEqualTo("2025-07-05");
        assertThat(other.getDetails()).isEqualTo("ZAKUP PRZY UZYCIU PAYPAL");
        assertThat(other.getTransactionMethod()).isEqualTo("OTHER");
        assertThat(other.getAmount()).isEqualTo("-100.00");
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