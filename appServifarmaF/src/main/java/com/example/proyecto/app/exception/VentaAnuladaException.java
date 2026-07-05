package com.example.proyecto.app.exception;

public class VentaAnuladaException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public VentaAnuladaException(String message) {
        super(message);
    }

    public VentaAnuladaException(String message, Throwable cause) {
        super(message, cause);
    }
}