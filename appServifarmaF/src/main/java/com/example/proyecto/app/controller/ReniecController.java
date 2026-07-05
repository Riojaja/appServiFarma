package com.example.proyecto.app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.proyecto.app.dto.response.PeruApiResponse;
import com.example.proyecto.app.dto.response.ReniecData;
import com.example.proyecto.app.dto.response.ReniecResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/reniec")
public class ReniecController {

    @Value("${peruapi.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public ReniecController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/consultar")
    public ResponseEntity<ReniecResponse> consultarReniec(@RequestParam String dni) {
        // Validar formato DNI (8 dígitos)
        if (dni == null || !dni.matches("\\d{8}")) {
            log.warn("DNI inválido recibido: {}", dni);
            return ResponseEntity.badRequest()
                    .body(new ReniecResponse(false, null, "DNI inválido. Debe tener 8 dígitos."));
        }

        try {
            String url = "https://peruapi.com/api/dni/" + dni + "?api_token=" + apiKey.trim();

            log.debug("🔍 Consultando PeruAPI con URL: {}", url);

            ResponseEntity<PeruApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    PeruApiResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                PeruApiResponse body = response.getBody();

                if ("200".equals(body.getCode()) && body.getDni() != null) {
                    ReniecData data = new ReniecData();
                    data.setDni(body.getDni());
                    data.setNombres(body.getNombres());
                    data.setApellidoPaterno(body.getApellidoPaterno());
                    data.setApellidoMaterno(body.getApellidoMaterno());

                    String nombreCompleto = body.getCliente();
                    if (nombreCompleto == null || nombreCompleto.isEmpty()) {
                        nombreCompleto = (body.getNombres() != null ? body.getNombres() : "") + " " +
                                         (body.getApellidoPaterno() != null ? body.getApellidoPaterno() : "") + " " +
                                         (body.getApellidoMaterno() != null ? body.getApellidoMaterno() : "");
                        nombreCompleto = nombreCompleto.trim();
                    }
                    data.setNombreCompleto(nombreCompleto);

                    log.info("✅ Datos obtenidos de PeruAPI para DNI: {}", dni);
                    return ResponseEntity.ok(new ReniecResponse(true, data, null));
                } else {
                    log.warn("⚠️ PeruAPI devolvió code: {} para DNI: {}", body.getCode(), dni);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ReniecResponse(false, null, "DNI no encontrado en RENIEC"));
                }
            } else {
                log.error("❌ Respuesta vacía o error de PeruAPI para DNI: {}", dni);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ReniecResponse(false, null, "Error al consultar RENIEC"));
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.warn("⛔ Límite de consultas alcanzado para DNI: {}", dni);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(new ReniecResponse(false, null, "Límite de consultas diarias alcanzado. Intente mañana."));
            }
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.error("❌ API Key inválida para DNI: {}", dni);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ReniecResponse(false, null, "API Key inválida. Verifique su suscripción."));
            }
            log.error("❌ Error HTTP al consultar PeruAPI para DNI {}: {}", dni, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ReniecResponse(false, null, "Error al consultar RENIEC: " + e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error interno al consultar RENIEC para DNI {}: {}", dni, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ReniecResponse(false, null, "Error interno al consultar RENIEC"));
        }
    }
}