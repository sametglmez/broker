package com.example.broker.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorType {

    CUSTOMER_NOT_FOUND(HttpStatus.NOT_FOUND, "Customer not found"),
    ASSET_NOT_FOUND(HttpStatus.NOT_FOUND, "Asset not found"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Order not found"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred"),
    INSUFFICIENT_FUNDS(HttpStatus.BAD_REQUEST, "Insufficient TRY usable balance"),
    INSUFFICIENT_ASSET(HttpStatus.BAD_REQUEST, "Insufficient asset usable balance"),
    ORDER_NOT_PENDING(HttpStatus.BAD_REQUEST, "Yalnızca PENDING durumundaki emirler iptal edilebilir."),
    TRY_ASSET_NOT_FOUND(HttpStatus.BAD_REQUEST, "TRY varlığı bulunamadı."),
    NO_ACCESS(HttpStatus.BAD_REQUEST, "You dont have access"),
    ROLE_NOT_FOUND(HttpStatus.BAD_REQUEST, "Role not found");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorType(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}