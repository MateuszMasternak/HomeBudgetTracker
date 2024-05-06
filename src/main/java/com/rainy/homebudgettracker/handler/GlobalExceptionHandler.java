package com.rainy.homebudgettracker.handler;

import com.rainy.homebudgettracker.handler.exception.EmailAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.ExpiredConfirmationTokenException;
import com.rainy.homebudgettracker.handler.exception.InvalidConfirmationTokenException;
import jakarta.mail.MessagingException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashSet;

import static com.rainy.homebudgettracker.handler.BusinessErrorCodes.*;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ExceptionResponse> handleException(LockedException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(ACCOUNT_LOCKED.getCode())
                                .businessErrorDescription(ACCOUNT_LOCKED.getDescription())
                                .error(e.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ExceptionResponse> handleException(DisabledException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(ACCOUNT_DISABLED.getCode())
                                .businessErrorDescription(ACCOUNT_DISABLED.getDescription())
                                .error(e.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleException(BadCredentialsException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(BAD_CREDENTIALS.getCode())
                                .businessErrorDescription(BAD_CREDENTIALS.getDescription())
                                .build()
                );
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ExceptionResponse> handleException(MessagingException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ExceptionResponse.builder()
                                .error(e.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleException(MethodArgumentNotValidException e) {
        var errors = new HashSet<String>();
        e.getBindingResult().getAllErrors().forEach(error -> errors.add(error.getDefaultMessage()));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(MISSING_OR_INVALID_REQUEST_BODY_ELEMENT.getCode())
                                .businessErrorDescription(MISSING_OR_INVALID_REQUEST_BODY_ELEMENT.getDescription())
                                .validationErrors(errors)
                                .build()
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception e) {
        log.error("Internal server error", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorDescription("Internal server error. Contact support")
                                .error(e.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ExceptionResponse> handleException(MissingServletRequestParameterException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(MISSING_REQUEST_PARAMETER.getCode())
                                .businessErrorDescription(
                                        MISSING_REQUEST_PARAMETER.getDescription() + ": " + e.getParameterName())
                                .build()
                );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> handleException(HttpMessageNotReadableException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(MISSING_REQUEST_BODY.getCode())
                                .businessErrorDescription(MISSING_REQUEST_BODY.getDescription())
                                .build()
                );
    }

    @ExceptionHandler(InvalidConfirmationTokenException.class)
    public ResponseEntity<ExceptionResponse> handleException(InvalidConfirmationTokenException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(INVALID_CONFIRMATION_TOKEN.getCode())
                                .businessErrorDescription(INVALID_CONFIRMATION_TOKEN.getDescription())
                                .error(e.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(ExpiredConfirmationTokenException.class)
    public ResponseEntity<ExceptionResponse> handleException(ExpiredConfirmationTokenException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(EXPIRED_CONFIRMATION_TOKEN.getCode())
                                .businessErrorDescription(EXPIRED_CONFIRMATION_TOKEN.getDescription())
                                .error(e.getMessage())
                                .build()
                );
    }
}
