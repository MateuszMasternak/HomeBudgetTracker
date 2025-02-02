package com.rainy.homebudgettracker.category;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.UUID;

public class TestData {
    public static final String USER_SUB = "550e8400-e29b-41d4-a716-446655440000";
    public static final String OTHER_USER_SUB = "550e8400-e29b-41d4-a716-446655440001";

    public static final UUID CATEGORY_ID_FOOD = UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100");
    public static final UUID OTHER_USER_CATEGORY_ID = UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903");
    public static final UUID ASSOCIATED_CATEGORY_ID = UUID.fromString("c7e2fa7e-2267-4da5-ade1-5dc79948a773");
    public static final UUID NON_EXISTENT_CATEGORY_ID = UUID.fromString("cb5f0153-5b1e-4f4b-9886-ae6791284043");
    public static final UUID CATEGORY_ID_TRANSPORT = UUID.fromString("312a1af8-a338-49ea-b67c-860062a10101");

    public static final Category CATEGORY_FOOD = new Category(CATEGORY_ID_FOOD, "Food", USER_SUB);
    public static final Category CATEGORY_OTHER_USER = new Category(
            OTHER_USER_CATEGORY_ID, "Healthcare", OTHER_USER_SUB);
    public static final Category CATEGORY_ASSOCIATED = new Category(
            ASSOCIATED_CATEGORY_ID, "Rent", USER_SUB);
    public static final Category CATEGORY_TRANSPORT = new Category(CATEGORY_ID_TRANSPORT, "Transport", USER_SUB);

    public static final CategoryRequest CATEGORY_REQUEST_TRANSPORT = new CategoryRequest("Transport");

    public static final CategoryResponse CATEGORY_RESPONSE_FOOD = new CategoryResponse(CATEGORY_ID_FOOD, "Food");
    public static final CategoryResponse CATEGORY_RESPONSE_TRANSPORT = new CategoryResponse(CATEGORY_ID_TRANSPORT, "Transport");

    public static final Pageable PAGEABLE = PageRequest.of(0, 10, Sort.by("name"));
}

