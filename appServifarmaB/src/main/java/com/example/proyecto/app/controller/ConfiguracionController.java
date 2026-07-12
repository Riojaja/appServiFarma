package com.example.proyecto.app.controller;

import com.example.proyecto.app.dto.response.MensajeResponse;
import com.example.proyecto.app.service.ConfiguracionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/configuracion")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    @GetMapping("/seguridad")
    public ResponseEntity<Map<String, String>> obtenerConfiguracionSeguridad() {
        return ResponseEntity.ok(configuracionService.obtenerTodas());
    }

    @PutMapping("/seguridad")
    public ResponseEntity<MensajeResponse> actualizarConfiguracionSeguridad(
            @RequestBody Map<String, String> configuraciones) {
        configuracionService.actualizarConfiguracion(configuraciones);
        return ResponseEntity.ok(new MensajeResponse("Configuración actualizada correctamente"));
    }
}