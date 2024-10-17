package com.rainy.homebudgettracker.images;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ImageCompressorTest {

    @Test
    void shouldReturnCompressedImage() throws IOException {
        File inputFile = new File("src/test/resources/test.png");
        File outputFile = ImageCompressor.compressImage(inputFile);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() < inputFile.length());
        assertTrue(outputFile.getName().endsWith(".jpg"));
    }
}