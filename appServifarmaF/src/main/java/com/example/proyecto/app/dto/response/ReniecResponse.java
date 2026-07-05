package com.example.proyecto.app.dto.response;

public class ReniecResponse {
    private boolean success;
    private ReniecData data;
    private String message;

    // Constructor vacío (necesario para Jackson)
    public ReniecResponse() {}

    // Constructor con parámetros
    public ReniecResponse(boolean success, ReniecData data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    // Getters y Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ReniecData getData() {
        return data;
    }

    public void setData(ReniecData data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}