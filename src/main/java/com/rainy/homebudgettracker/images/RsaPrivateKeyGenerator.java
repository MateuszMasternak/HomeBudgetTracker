package com.rainy.homebudgettracker.images;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class RsaPrivateKeyGenerator {
    public static RSAPrivateKey generatePrivateKeyFromString(String privateKey) {
        String privateKeyPEMCleaned = cleanPrivateKey(privateKey);
        byte[] privateKeyBytes = getDecodedPrivateKey(privateKeyPEMCleaned);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Failed to generate the private key", e);
        }
    }

    public static RSAPrivateKey generatePrivateKeyFromFile(String filePath) {
        try {
            String privateKey = getEncodedPrivateKey(filePath);
            return generatePrivateKeyFromString(privateKey);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read private key from file", e);
        }
    }

    private static byte[] getDecodedPrivateKey(String privateKey) {
        return Base64.getDecoder().decode(privateKey);
    }

    private static String getEncodedPrivateKey(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    private static String cleanPrivateKey(String privateKey) {
        privateKey = privateKey.replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        return privateKey;
    }
}
