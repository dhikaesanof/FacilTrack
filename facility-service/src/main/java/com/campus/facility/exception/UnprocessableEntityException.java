package com.campus.facility.exception;

public class UnprocessableEntityException extends RuntimeException {
    public UnprocessableEntityException(String message) {
        super(message);
    }
}
