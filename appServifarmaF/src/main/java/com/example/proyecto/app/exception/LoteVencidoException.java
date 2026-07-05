package com.example.proyecto.app.exception;

public class LoteVencidoException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public LoteVencidoException(String message) {
        super(message);
    }

    public LoteVencidoException(String message, Throwable cause) {
        super(message, cause);
    }
}