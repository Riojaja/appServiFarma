package com.example.proyecto.app.exception;

public class MovimientoInvalidoException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public MovimientoInvalidoException(String message) {
        super(message);
    }

    public MovimientoInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
