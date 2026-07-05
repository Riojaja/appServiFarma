package com.example.proyecto.app.exception;

public class DuplicadoException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public DuplicadoException(String message) {
        super(message);
    }

    public DuplicadoException(String message, Throwable cause) {
        super(message, cause);
    }
}