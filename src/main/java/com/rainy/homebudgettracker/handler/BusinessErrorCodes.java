package com.rainy.homebudgettracker.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;


@Getter
public enum BusinessErrorCodes {

    MISSING_OR_INVALID_REQUEST_BODY_ELEMENT(401, BAD_REQUEST, "Missing or invalid request body element"),
    MISSING_REQUEST_PARAMETER(402, BAD_REQUEST, "Missing request parameter"),
    MISSING_REQUEST_BODY(403, BAD_REQUEST, "Missing request body"),
    RECORD_IS_NOT_REACHABLE(404, NOT_FOUND, "Record does not exist or is not accessible"),
    RECORD_ALREADY_EXISTS(405, CONFLICT, "Record already exists"),
    RECORD_ASSOCIATED_WITH_ANOTHER_RECORD(406, CONFLICT, "Record is associated with other records"),
    INVALID_FILE(407, UNSUPPORTED_MEDIA_TYPE, "Invalid file"),
    INVALID_FILE_FORMAT(409, UNSUPPORTED_MEDIA_TYPE, "Invalid file format"),
    FILE_PROCESSING_ERROR(410, HttpStatus.INTERNAL_SERVER_ERROR, "File processing error. Please try again later or contact support"),
    PREMIUM_STATUS_REQUIRED(408, UNAUTHORIZED, "Premium status required"),
    MAXIMUM_FILE_SIZE_EXCEEDED(411, PAYLOAD_TOO_LARGE, "Maximum file size exceeded. Maximum file size is 10MB"),
    EXCHANGE_RATE_API_QUOTA_REACHED(412, FAILED_DEPENDENCY, "Quota exceeded. Enter custom exchange rate or try later"),
    EXCHANGE_RATE_API_ERROR(413, FAILED_DEPENDENCY, "Exchange rate API error"),
    INTERNAL_SERVER_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error. Please contact support"),
    FILE_UPLOAD_ERROR(501, HttpStatus.INTERNAL_SERVER_ERROR, "File upload error. Please try again later or contact support"),
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
