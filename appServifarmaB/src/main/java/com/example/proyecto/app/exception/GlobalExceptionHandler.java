package com.example.proyecto.app.exception;

import com.example.proyecto.app.dto.response.MensajeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ==============================
    // EXCEPCIONES DE NEGOCIO (HTTP 400 y 409)
    // ==============================

    /**
     * Maneja excepciones de negocio genéricas (BusinessException).
     * Retorna HTTP 400 BAD REQUEST.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<MensajeResponse> handleBusinessException(BusinessException ex) {
        log.warn("Error de negocio: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MensajeResponse(ex.getMessage()));
    }

    /**
     * Maneja excepciones de duplicidad (DuplicadoException).
     * Retorna HTTP 409 CONFLICT.
     */
    @ExceptionHandler(DuplicadoException.class)
    public ResponseEntity<MensajeResponse> handleDuplicadoException(DuplicadoException ex) {
        log.warn("Conflicto de duplicidad: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new MensajeResponse(ex.getMessage()));
    }

    /**
     * Maneja excepciones de recurso no encontrado (ResourceNotFoundException).
     * Retorna HTTP 404 NOT FOUND.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<MensajeResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MensajeResponse(ex.getMessage()));
    }

    /**
     * Maneja excepciones de autenticación (AuthException).
     * Retorna HTTP 401 UNAUTHORIZED.
     */
    @ExceptionHandler(com.example.proyecto.app.auth.AuthException.class)
    public ResponseEntity<MensajeResponse> handleAuthException(com.example.proyecto.app.auth.AuthException ex) {
        log.warn("Error de autenticación: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MensajeResponse(ex.getMessage()));
    }

    /**
     * Maneja excepciones de permiso denegado propias del negocio (PermisoDenegadoException).
     * Retorna HTTP 403 FORBIDDEN.
     */
    @ExceptionHandler(PermisoDenegadoException.class)
    public ResponseEntity<MensajeResponse> handlePermisoDenegadoException(PermisoDenegadoException ex) {
        log.warn("Permiso denegado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MensajeResponse(ex.getMessage()));
    }

    /**
     * Maneja el rechazo de Spring Security cuando un @PreAuthorize bloquea el acceso
     * (ej. un "vendedor" intentando eliminar un producto o entrar a /api/auditoria).
     * Sin este handler, la excepción caía en handleGenericException() y el usuario
     * recibía un 500 genérico en vez de un 403 con mensaje claro.
     * Retorna HTTP 403 FORBIDDEN.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MensajeResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MensajeResponse("No tienes permisos para realizar esta acción."));
    }

    // ==============================
    // EXCEPCIONES DE VALIDACIÓN (HTTP 400)
    // ==============================

    /**
     * Maneja errores de validación de DTOs con @Valid.
     * Retorna HTTP 400 BAD REQUEST con un mapa de errores por campo.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Errores de validación: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Maneja errores de formato de JSON mal estructurado.
     * Retorna HTTP 400 BAD REQUEST.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MensajeResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("Error en el formato del JSON: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MensajeResponse("El formato del JSON enviado es incorrecto. Verifica la estructura de los datos."));
    }

    // ==============================
    // EXCEPCIONES NO CONTROLADAS (HTTP 500)
    // ==============================

    /**
     * Maneja cualquier otra excepción no capturada por los handlers específicos.
     * Retorna HTTP 500 INTERNAL SERVER ERROR con un mensaje genérico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MensajeResponse> handleGenericException(Exception ex) {
        log.error("Error interno del servidor: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MensajeResponse("Ocurrió un error interno en el servidor. Intente más tarde."));
    }
}