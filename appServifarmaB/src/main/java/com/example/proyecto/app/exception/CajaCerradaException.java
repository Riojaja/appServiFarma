package com.example.proyecto.app.exception;

public class CajaCerradaException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public CajaCerradaException(String message) {
        super(message);
    }

    public CajaCerradaException(String message, Throwable cause) {
        super(message, cause);
    }
}