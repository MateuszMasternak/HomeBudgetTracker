package com.rainy.homebudgettracker.images;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class CloudfrontServiceTest {
    @InjectMocks
    CloudfrontService cloudfrontService;
    @Mock
    CloudfrontUrlRepository cloudfrontUrlRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(cloudfrontService, "cloudfrontUrl", "https://test.cloudfront.net/");
        ReflectionTestUtils.setField(cloudfrontService, "publicKeyPairId", "test-key-id");
        ReflectionTestUtils.setField(cloudfrontService, "privateKey", "test-private-key");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getGetUrl() {
        String key = "test-key";
        String actual = cloudfrontService.createGetUrl(key);

        String expected = "https://test.cloudfront.net/test-key";
        assertEquals(expected, actual);
    }

    @Test
    void createSignedGetURL() {
    }
}