package com.example.proyecto.app.exception;

import com.example.proyecto.app.dto.response.MensajeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<MensajeResponse> handleBusinessException(BusinessException ex) {
        log.warn("Error de negocio: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MensajeResponse(ex.getMessage()));
    }

    @ExceptionHandler(DuplicadoException.class)
    public ResponseEntity<MensajeResponse> handleDuplicadoException(DuplicadoException ex) {
        log.warn("Conflicto de duplicidad: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new MensajeResponse(ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<MensajeResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MensajeResponse(ex.getMessage()));
    }

    @ExceptionHandler(com.example.proyecto.app.auth.AuthException.class)
    public ResponseEntity<MensajeResponse> handleAuthException(com.example.proyecto.app.auth.AuthException ex) {
        log.warn("Error de autenticación: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MensajeResponse(ex.getMessage()));
    }

    @ExceptionHandler(PermisoDenegadoException.class)
    public ResponseEntity<MensajeResponse> handlePermisoDenegadoException(PermisoDenegadoException ex) {
        log.warn("Permiso denegado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MensajeResponse(ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MensajeResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MensajeResponse("No tienes permisos para realizar esta acción."));
    }

    // ==============================
    // EXCEPCIONES DE INTEGRIDAD DE BASE DE DATOS (HTTP 409)
    // ==============================

    /**
     * Maneja violaciones de integridad referencial (ej. eliminar un lote con movimientos de stock).
     * Retorna HTTP 409 CONFLICT con un mensaje claro.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<MensajeResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos: {}", ex.getMessage());

        // Mensaje personalizado para el usuario
        String mensaje = "No se puede eliminar este registro porque tiene información relacionada en el sistema.";

        // Opcional: puedes personalizar según el mensaje de la excepción
        if (ex.getMessage() != null && ex.getMessage().contains("movimientos_stock")) {
            mensaje = "No se puede eliminar este lote porque tiene movimientos de stock asociados.";
        } else if (ex.getMessage() != null && ex.getMessage().contains("detalle_ventas")) {
            mensaje = "No se puede eliminar este lote porque tiene ventas asociadas.";
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new MensajeResponse(mensaje));
    }

    // ==============================
    // EXCEPCIONES DE VALIDACIÓN (HTTP 400)
    // ==============================

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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MensajeResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("Error en el formato del JSON: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MensajeResponse("El formato del JSON enviado es incorrecto. Verifica la estructura de los datos."));
    }

    // ==============================
    // EXCEPCIONES NO CONTROLADAS (HTTP 500)
    // ==============================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MensajeResponse> handleGenericException(Exception ex) {
        log.error("Error interno del servidor: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MensajeResponse("Ocurrió un error interno en el servidor. Intente más tarde."));
    }
}