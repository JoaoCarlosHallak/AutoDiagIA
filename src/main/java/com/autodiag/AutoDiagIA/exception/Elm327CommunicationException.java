package com.autodiag.AutoDiagIA.exception;

import java.io.IOException;


public class Elm327CommunicationException extends IOException {


    public Elm327CommunicationException(String message) {
        super(message);
    }

    public Elm327CommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}









