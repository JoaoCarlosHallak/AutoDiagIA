package com.autodiag.AutoDiagIA.exception;

public class InvalidObdRequestException extends RuntimeException {

    public InvalidObdRequestException(String message) {
        super(message);
    }
}