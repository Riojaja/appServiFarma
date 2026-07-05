package com.example.proyecto.app.dto.request;

public class CorreoRequest {
    private String destino;
    private String asunto;
    private String mensaje;

    // Getters y Setters
    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}