package com.example.proyecto.app.exception;

public class ParametroInvalidoException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public ParametroInvalidoException(String message) {
        super(message);
    }

    public ParametroInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}