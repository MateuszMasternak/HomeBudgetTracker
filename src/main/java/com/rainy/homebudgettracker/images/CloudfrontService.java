package com.rainy.homebudgettracker.images;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;

import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudfrontService {
    private final CloudfrontUrlRepository cloudfrontUrlRepository;
    @Value("${aws.cloudfront.url}")
    private String cloudfrontUrl;
    @Value("${aws.cloudfront.public-key-pair-id}")
    private String publicKeyPairId;
    @Value("${aws.cloudfront.private-key}")
    private String privateKey;

    public String createGetUrl (String key) {
        if (key == null) {
            log.error("Null key provided for creating get URL");
            throw new IllegalArgumentException("Key cannot be null");
        }

        return cloudfrontUrl + key;
    }

    public String createSignedGetURL(String key) {
        if (key == null) {
            log.error("Null key provided for creating signed URL");
            throw new IllegalArgumentException("Key cannot be null");
        }

        Optional<CloudfrontUrl> cloudfrontUrl = cloudfrontUrlRepository.findByKey(key);
        if (hasValidUrl(cloudfrontUrl)) {
            return cloudfrontUrl.get().getUrl();
        }

        CannedSignerRequest signerRequest = createCannedSignerRequest(key);

        SignedUrl signedUrl = CloudFrontUtilities.create().getSignedUrlWithCannedPolicy(signerRequest);
        String signedUrlString = signedUrl.url();

        saveCloudfrontUrl(key, signedUrlString, signerRequest.expirationDate());

        return signedUrl.url();
    }

    private CannedSignerRequest createCannedSignerRequest(String key) {
        if (key == null) {
            log.error("Null key provided for creating canned signer request");
            throw new IllegalArgumentException("Key cannot be null");
        }

        RSAPrivateKey rsaPrivateKey;
        rsaPrivateKey = RsaPrivateKeyGenerator.generatePrivateKeyFromString(privateKey);

        Instant expirationDate = Instant.now().plus(1, ChronoUnit.DAYS);

        return CannedSignerRequest.builder()
                .resourceUrl(createGetUrl(key))
                .privateKey(rsaPrivateKey)
                .keyPairId(publicKeyPairId)
                .expirationDate(expirationDate)
                .build();
    }

    private boolean hasValidUrl(Optional<CloudfrontUrl> cloudfrontUrl) {
        boolean hasValid =  cloudfrontUrl.filter(url -> !url.getExpirationTime().isBefore(
                Instant.now().plus(30, ChronoUnit.MINUTES))).isPresent();

        if (!hasValid) {
            cloudfrontUrl.ifPresent(cloudfrontUrlRepository::delete);
        }

        return hasValid;
    }

    private void saveCloudfrontUrl(String key, String url, Instant expirationTime) {
        CloudfrontUrl cloudfrontUrl = CloudfrontUrl.builder()
                .key(key)
                .url(url)
                .expirationTime(expirationTime)
                .build();

        cloudfrontUrlRepository.save(cloudfrontUrl);
    }
}
