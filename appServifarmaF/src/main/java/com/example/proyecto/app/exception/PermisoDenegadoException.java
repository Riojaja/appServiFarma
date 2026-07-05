package com.example.proyecto.app.exception;

public class PermisoDenegadoException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public PermisoDenegadoException(String message) {
        super(message);
    }

    public PermisoDenegadoException(String message, Throwable cause) {
        super(message, cause);
    }
}