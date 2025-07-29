package com.rainy.homebudgettracker.handler;

import com.rainy.homebudgettracker.handler.exception.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashSet;
import java.util.UUID;

import static com.rainy.homebudgettracker.handler.BusinessErrorCodes.*;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    // ResponseStatus annotation is used for Swagger to generate the documentation

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception e) {
        UUID errorId = UUID.randomUUID();
        String message = "Internal server error. Error ID: %s".formatted(errorId.toString());
        log.error(message, e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorDescription(INTERNAL_SERVER_ERROR.getDescription() + ". Error ID: " + errorId)
                                .build()
                );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleException(MethodArgumentNotValidException e) {
        var errors = new HashSet<String>();
        e.getBindingResult().getAllErrors().forEach(error -> errors.add(error.getDefaultMessage()));
        return ResponseEntity
                .status(MISSING_OR_INVALID_REQUEST_BODY_ELEMENT.getCode())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(MISSING_OR_INVALID_REQUEST_BODY_ELEMENT.getCode())
                                .businessErrorDescription(MISSING_OR_INVALID_REQUEST_BODY_ELEMENT.getDescription())
                                .validationErrors(errors)
                                .build()
                );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ExceptionResponse> handleException(MissingServletRequestParameterException e) {
        return ResponseEntity
                .status(MISSING_REQUEST_PARAMETER.getCode())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(MISSING_REQUEST_PARAMETER.getCode())
                                .businessErrorDescription(
                                        MISSING_REQUEST_PARAMETER.getDescription() + ": " + e.getParameterName())
                                .build()
                );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> handleException(HttpMessageNotReadableException e) {
        return ResponseEntity
                .status(MISSING_REQUEST_BODY.getCode())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(MISSING_REQUEST_BODY.getCode())
                                .businessErrorDescription(MISSING_REQUEST_BODY.getDescription())
                                .build()
                );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(RecordDoesNotExistException.class)
    public ResponseEntity<ExceptionResponse> handleException(RecordDoesNotExistException e) {
        return ResponseEntity
                .status(RECORD_IS_NOT_REACHABLE.getCode())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(RECORD_IS_NOT_REACHABLE.getCode())
                                .businessErrorDescription(RECORD_IS_NOT_REACHABLE.getDescription())
                                .build()
                );
    }

    // The same what in the previous method to not let the user know if the record exists
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserIsNotOwnerException.class)
    public ResponseEntity<ExceptionResponse> handleException(UserIsNotOwnerException e) {
        return ResponseEntity
                .status(RECORD_IS_NOT_REACHABLE.getCode())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(RECORD_IS_NOT_REACHABLE.getCode())
                                .businessErrorDescription(RECORD_IS_NOT_REACHABLE.getDescription())
                                .build()
                );
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(RecordAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponse> handleException(RecordAlreadyExistsException e) {
        return ResponseEntity
                .status(RECORD_ALREADY_EXISTS.getCode())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(RECORD_ALREADY_EXISTS.getCode())
                                .businessErrorDescription(RECORD_ALREADY_EXISTS.getDescription())
                                .error(e.getMessage())
                                .build()
                );
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(CategoryAssociatedWithTransactionException.class)
    public ResponseEntity<ExceptionResponse> handleException(CategoryAssociatedWithTransactionException e) {
        return ResponseEntity
                .status(RECORD_ASSOCIATED_WITH_ANOTHER_RECORD.getCode())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(RECORD_ASSOCIATED_WITH_ANOTHER_RECORD.getCode())
                                .businessErrorDescription(RECORD_ASSOCIATED_WITH_ANOTHER_RECORD.getDescription())
                                .error(e.getMessage())
                                .build()
                );
    }

    @ResponseStatus(HttpStatus.FAILED_DEPENDENCY)
    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public ResponseEntity<ExceptionResponse> handleException(HttpClientErrorException.TooManyRequests e)
    {
        return ResponseEntity
                .status(EXCHANGE_RATE_API_QUOTA_REACHED.getCode())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(EXCHANGE_RATE_API_QUOTA_REACHED.getCode())
                                .businessErrorDescription(EXCHANGE_RATE_API_QUOTA_REACHED.getDescription())
                                .build()
                );
    }

    @ResponseStatus(HttpStatus.FAILED_DEPENDENCY)
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ExceptionResponse> handleException(HttpClientErrorException e) {
        return ResponseEntity
                .status(EXCHANGE_RATE_API_ERROR.getCode())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(EXCHANGE_RATE_API_ERROR.getCode())
                                .businessErrorDescription(EXCHANGE_RATE_API_ERROR.getDescription())
                                .error(e.getMessage())
                                .build()
                );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ExceptionResponse> handleException(HttpServerErrorException e) {
        return ResponseEntity
                .status(EXCHANGE_RATE_API_ERROR.getCode())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(EXCHANGE_RATE_API_ERROR.getCode())
                                .businessErrorDescription(EXCHANGE_RATE_API_ERROR.getDescription())
                                .error(e.getMessage())
                                .build()
                );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<ExceptionResponse> handleException(ImageUploadException e) {
        return ResponseEntity
                .status(FILE_UPLOAD_ERROR.getCode())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(FILE_UPLOAD_ERROR.getCode())
                                .businessErrorDescription(FILE_UPLOAD_ERROR.getDescription())
                                .error(e.getMessage())
                                .build()
                );
    }

    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(WrongFileTypeException.class)
    public ResponseEntity<ExceptionResponse> handleException(WrongFileTypeException e) {
        return ResponseEntity
                .status(INVALID_FILE.getCode())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(INVALID_FILE.getCode())
                                .businessErrorDescription(INVALID_FILE.getDescription())
                                .error(e.getMessage())
                                .build()
                );
    }

    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ExceptionResponse> handleException(MaxUploadSizeExceededException e) {

        return ResponseEntity
                .status(MAXIMUM_FILE_SIZE_EXCEEDED.getCode())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(MAXIMUM_FILE_SIZE_EXCEEDED.getCode())
                                .businessErrorDescription(MAXIMUM_FILE_SIZE_EXCEEDED.getDescription())
                                .build()
                );
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(PremiumStatusRequiredException.class)
    public ResponseEntity<ExceptionResponse> handleException(PremiumStatusRequiredException e) {
        return ResponseEntity
                .status(PREMIUM_STATUS_REQUIRED.getCode())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(PREMIUM_STATUS_REQUIRED.getCode())
                                .businessErrorDescription(PREMIUM_STATUS_REQUIRED.getDescription())
                                .error(e.getMessage())
                                .build()
                );
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(WrongFileFormatException.class)
    public ResponseEntity<ExceptionResponse> handleException(WrongFileFormatException e) {
        return ResponseEntity
                .status(INVALID_FILE_FORMAT.getCode())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(INVALID_FILE_FORMAT.getCode())
                                .businessErrorDescription(INVALID_FILE_FORMAT.getDescription())
                                .error(e.getMessage())
                                .build()
                );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<ExceptionResponse> handleException(FileProcessingException e) {
        return ResponseEntity
                .status(FILE_PROCESSING_ERROR.getCode())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(FILE_PROCESSING_ERROR.getCode())
                                .businessErrorDescription(FILE_PROCESSING_ERROR.getDescription())
                                .error(e.getMessage())
                                .build()
                );
    }
}
