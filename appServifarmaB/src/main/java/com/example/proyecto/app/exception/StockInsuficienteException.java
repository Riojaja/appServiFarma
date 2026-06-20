package com.example.proyecto.app.exception;

public class StockInsuficienteException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public StockInsuficienteException(String message) {
        super(message);
    }

    public StockInsuficienteException(String message, Throwable cause) {
        super(message, cause);
    }
}
