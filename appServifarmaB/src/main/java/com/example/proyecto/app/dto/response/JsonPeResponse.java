package com.example.proyecto.app.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JsonPeResponse {
    private boolean success;
    private String message;
    private JsonPeData data;

    // Getters y Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JsonPeData getData() {
        return data;
    }

    public void setData(JsonPeData data) {
        this.data = data;
    }

    public static class JsonPeData {
        private String dni;
        private String nombres;
        @JsonProperty("apellido_paterno")
        private String apellidoPaterno;
        @JsonProperty("apellido_materno")
        private String apellidoMaterno;
        @JsonProperty("nombre_completo")
        private String nombreCompleto;

        // Getters y Setters
        public String getDni() {
            return dni;
        }

        public void setDni(String dni) {
            this.dni = dni;
        }

        public String getNombres() {
            return nombres;
        }

        public void setNombres(String nombres) {
            this.nombres = nombres;
        }

        public String getApellidoPaterno() {
            return apellidoPaterno;
        }

        public void setApellidoPaterno(String apellidoPaterno) {
            this.apellidoPaterno = apellidoPaterno;
        }

        public String getApellidoMaterno() {
            return apellidoMaterno;
        }

        public void setApellidoMaterno(String apellidoMaterno) {
            this.apellidoMaterno = apellidoMaterno;
        }

        public String getNombreCompleto() {
            return nombreCompleto;
        }

        public void setNombreCompleto(String nombreCompleto) {
            this.nombreCompleto = nombreCompleto;
        }
    }
}