package com.campus.report.exception;

public class UnprocessableEntityException extends RuntimeException {
    public UnprocessableEntityException(String message) {
        super(message);
    }
}
