package com.autodiag.AutoDiagIA.exception;

public class InvalidObdRequestException extends InvalidRequestException {

    public InvalidObdRequestException(String message) {
        super(message);
    }

    public InvalidObdRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}