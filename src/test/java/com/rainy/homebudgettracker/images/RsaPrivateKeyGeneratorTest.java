package com.rainy.homebudgettracker.images;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class RsaPrivateKeyGeneratorTest {

    @Test
    void shouldReturnPrivateKeyFromString() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String key = generatePemPrivateKey();
        RSAPrivateKey rsaPrivateKey = RsaPrivateKeyGenerator.generatePrivateKeyFromString(key);

        assertNotNull(rsaPrivateKey);
        assertEquals("RSA", rsaPrivateKey.getAlgorithm());
        assertEquals("PKCS#8", rsaPrivateKey.getFormat());
    }

    @Test
    void shouldReturnRsaPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String key = generatePemPrivateKey();
        String tempFilePath = savePrivateKeyToFile(key);

        RSAPrivateKey rsaPrivateKey = RsaPrivateKeyGenerator.generatePrivateKeyFromFile(tempFilePath);

        assertNotNull(rsaPrivateKey);
        assertEquals("RSA", rsaPrivateKey.getAlgorithm());
        assertEquals("PKCS#8", rsaPrivateKey.getFormat());
    }

    String generatePemPrivateKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        return convertPrivateKeyToPem((RSAPrivateKey) keyPair.getPrivate());
    }

    String convertPrivateKeyToPem(RSAPrivateKey rsaPrivateKey) {
        return "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getEncoder().encodeToString(rsaPrivateKey.getEncoded())
                + "\n-----END PRIVATE KEY-----";
    }

    String savePrivateKeyToFile(String key) throws IOException {
        File file = File.createTempFile("temp", ".pem");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(key);
        fileWriter.close();
        file.deleteOnExit();
        return file.getPath();
    }
}