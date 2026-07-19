package com.example.proyecto.app.service;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface ConfiguracionService {
    String getValor(String clave);
    Integer getValorInteger(String clave);
    void setValor(String clave, String valor);
    Map<String, String> obtenerTodas();
    void actualizarConfiguracion(Map<String, String> configuraciones);
    List<LocalTime> getHorasCierreTurno();
}