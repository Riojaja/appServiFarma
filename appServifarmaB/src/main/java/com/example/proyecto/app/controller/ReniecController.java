package com.example.proyecto.app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.proyecto.app.dto.response.JsonPeResponse;
import com.example.proyecto.app.dto.response.ReniecData;
import com.example.proyecto.app.dto.response.ReniecResponse;
import com.example.proyecto.app.service.ReniecCacheService;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/reniec")
public class ReniecController {

    @Value("${jsonpe.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ReniecCacheService cacheService;

    public ReniecController(RestTemplate restTemplate, ReniecCacheService cacheService) {
        this.restTemplate = restTemplate;
        this.cacheService = cacheService;
    }

    @GetMapping("/consultar")
    public ResponseEntity<ReniecResponse> consultarReniec(@RequestParam String dni) {
        // Validar formato DNI (8 dígitos)
        if (dni == null || !dni.matches("\\d{8}")) {
            log.warn("DNI inválido recibido: {}", dni);
            return ResponseEntity.badRequest()
                    .body(new ReniecResponse(false, null, "DNI inválido. Debe tener 8 dígitos."));
        }

        // ✅ 1. Verificar caché primero
        ReniecData cachedData = cacheService.get(dni);
        if (cachedData != null) {
            log.info("✅ DNI {} obtenido desde caché", dni);
            return ResponseEntity.ok(new ReniecResponse(true, cachedData, null));
        }

        // ✅ 2. Si no está en caché, consultar a JSON.pe
        try {
            String url = "https://api.json.pe/api/dni";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey.trim());

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("dni", dni);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            log.debug("🔍 Consultando JSON.pe con DNI: {}", dni);

            ResponseEntity<JsonPeResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    JsonPeResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonPeResponse body = response.getBody();

                if (body.isSuccess() && body.getData() != null) {
                    JsonPeResponse.JsonPeData data = body.getData();

                    ReniecData reniecData = new ReniecData();
                    reniecData.setDni(data.getDni());
                    reniecData.setNombres(data.getNombres());
                    reniecData.setApellidoPaterno(data.getApellidoPaterno());
                    reniecData.setApellidoMaterno(data.getApellidoMaterno());
                    reniecData.setNombreCompleto(data.getNombreCompleto());

                    // ✅ 3. Guardar en caché para futuras consultas
                    cacheService.put(dni, reniecData);

                    log.info("✅ Datos obtenidos de JSON.pe para DNI: {}", dni);
                    return ResponseEntity.ok(new ReniecResponse(true, reniecData, null));
                } else {
                    String msg = body.getMessage() != null ? body.getMessage() : "DNI no encontrado en RENIEC";
                    log.warn("⚠️ JSON.pe devolvió error: {}", msg);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ReniecResponse(false, null, msg));
                }
            } else {
                log.error("❌ Respuesta vacía o error de JSON.pe para DNI: {}", dni);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ReniecResponse(false, null, "Error al consultar RENIEC"));
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.warn("⛔ Límite de consultas alcanzado para DNI: {}", dni);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(new ReniecResponse(false, null, "Límite de consultas alcanzado. Intente más tarde."));
            }
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                log.error("❌ Token inválido para DNI: {}", dni);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ReniecResponse(false, null, "Token de API inválido. Verifique su suscripción."));
            }
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("⚠️ DNI no encontrado en RENIEC: {}", dni);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ReniecResponse(false, null, "DNI no encontrado en RENIEC"));
            }
            log.error("❌ Error HTTP al consultar JSON.pe para DNI {}: {}", dni, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ReniecResponse(false, null, "Error al consultar RENIEC: " + e.getMessage()));

        } catch (Exception e) {
            log.error("❌ Error interno al consultar RENIEC para DNI {}: {}", dni, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ReniecResponse(false, null, "Error interno al consultar RENIEC"));
        }
    }
}