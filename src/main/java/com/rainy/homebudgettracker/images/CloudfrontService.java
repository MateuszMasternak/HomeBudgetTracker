package com.rainy.homebudgettracker.images;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CloudfrontService {
    @Value("${aws.cloudfront.url}")
    private String cloudfrontUrl;

    public String createGetUrl (String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        return cloudfrontUrl + key;
    }
}
