package com.rainy.homebudgettracker.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;


@Getter
public enum BusinessErrorCodes {

    NO_CODE(0, NOT_IMPLEMENTED, "No code"),
    BAD_CREDENTIALS(301, UNAUTHORIZED, "Bad credentials"),
    ACCOUNT_DISABLED(302, FORBIDDEN, "Account disabled"),
    ACCOUNT_LOCKED(303, FORBIDDEN, "Account locked"),
    INVALID_CONFIRMATION_TOKEN(304, UNAUTHORIZED, "Invalid confirmation token"),
    EXPIRED_CONFIRMATION_TOKEN(305, UNAUTHORIZED, "Expired confirmation token"),
    MISSING_OR_INVALID_REQUEST_BODY_ELEMENT(306, BAD_REQUEST, "Missing or invalid request body element"),
    MISSING_REQUEST_PARAMETER(400, BAD_REQUEST, "Missing request parameter"),
    MISSING_REQUEST_BODY(401, BAD_REQUEST, "Missing request body"),
    INVALID_DELETE_REQUEST(402, NOT_FOUND, "Record does not exist or is not accessible"),
    INVALID_POST_REQUEST(403, BAD_REQUEST, "Record already exists"),
    RECORD_ASSOCIATED_WITH_ANOTHER_RECORD(404, BAD_REQUEST, "Record is associated with other records"),
    EXCHANGE_RATE_API_QUOTA_REACHED(405, BAD_REQUEST, "Quota exceeded. Enter custom exchange rate or try later"),
    EXCHANGE_RATE_API_ERROR(406, BAD_REQUEST, "Exchange rate API error"),
    INVALID_FILE(407, BAD_REQUEST, "Invalid file"),
    FILE_UPLOAD_ERROR(408, BAD_REQUEST, "File upload error"),
    MAXIMUM_FILE_SIZE_EXCEEDED(409, BAD_REQUEST, "Maximum file size exceeded. Maximum file size is 10MB"),
    ;

    private final int code;
    private final String description;
    private final HttpStatus httpStatus;

    BusinessErrorCodes(int code, HttpStatus httpStatus, String description) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.description = description;
    }
}
