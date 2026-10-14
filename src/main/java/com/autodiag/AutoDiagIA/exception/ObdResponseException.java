package com.autodiag.AutoDiagIA.exception;



public class ObdResponseException  extends RuntimeException {
    public ObdResponseException(String message) {
        super(message);
    }

    public ObdResponseException(String message, Throwable cause) {
        super(message, cause);
    }
    

    
}
