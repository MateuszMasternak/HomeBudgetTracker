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
