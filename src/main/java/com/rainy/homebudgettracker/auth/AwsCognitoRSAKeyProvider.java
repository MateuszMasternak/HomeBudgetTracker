package com.rainy.homebudgettracker.auth;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Component
@RequiredArgsConstructor
public class AwsCognitoRSAKeyProvider implements RSAKeyProvider {
    private final JwkProvider provider;

    @Override
    public RSAPublicKey getPublicKeyById(String s) {
        try {
            return (RSAPublicKey) provider.get(s).getPublicKey();
        } catch (JwkException e) {
            throw new RuntimeException("Failed to get RSA public key from AWS Cognito", e);
        }
    }

    @Override
    public RSAPrivateKey getPrivateKey() {
        return null;
    }

    @Override
    public String getPrivateKeyId() {
        return "";
    }
}
