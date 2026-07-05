package com.example.proyecto.app.controller;

import com.example.proyecto.app.dto.request.CorreoRequest;
import com.example.proyecto.app.dto.request.VentaRequest;
import com.example.proyecto.app.dto.response.DetalleVentaResponse;
import com.example.proyecto.app.dto.response.MensajeResponse;
import com.example.proyecto.app.dto.response.VentaResponse;
import com.example.proyecto.app.entity.Venta;
import com.example.proyecto.app.service.EmailService;
import com.example.proyecto.app.service.PdfGeneratorService;
import com.example.proyecto.app.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;
    private final PdfGeneratorService pdfGeneratorService;
    private final EmailService emailService;

    // ==============================
    // OPERACIÓN PRINCIPAL: REGISTRAR VENTA
    // ==============================

    @PostMapping
    public ResponseEntity<VentaResponse> registrarVenta(@Valid @RequestBody VentaRequest request) {
        log.debug("Solicitud de registro de venta para usuario ID: {}", request.getUsuarioId());
        VentaResponse response = ventaService.registrarVenta(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ==============================
    // ANULACIÓN DE VENTAS
    // ==============================

    @PatchMapping("/{id}/anular")
    public ResponseEntity<MensajeResponse> anularVenta(@PathVariable Integer id) {
        log.debug("Solicitud de anulación de venta con ID: {}", id);
        ventaService.anularVenta(id);
        return ResponseEntity.ok(new MensajeResponse("Venta anulada correctamente"));
    }

    // ==============================
    // CONSULTAS DE VENTAS
    // ==============================

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponse> obtenerVentaPorId(@PathVariable Integer id) {
        log.debug("Solicitud de venta con ID: {}", id);
        VentaResponse response = ventaService.obtenerVentaPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<VentaResponse>> listarTodas() {
        log.debug("Solicitud de listado de todas las ventas");
        List<VentaResponse> responses = ventaService.listarTodas();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<VentaResponse>> listarPorCliente(@PathVariable Integer clienteId) {
        log.debug("Solicitud de ventas del cliente ID: {}", clienteId);
        List<VentaResponse> responses = ventaService.listarPorCliente(clienteId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<VentaResponse>> listarPorUsuario(@PathVariable Integer usuarioId) {
        log.debug("Solicitud de ventas del usuario ID: {}", usuarioId);
        List<VentaResponse> responses = ventaService.listarPorUsuario(usuarioId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/fechas")
    public ResponseEntity<List<VentaResponse>> listarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.debug("Solicitud de ventas entre {} y {}", inicio, fin);
        List<VentaResponse> responses = ventaService.listarPorFecha(inicio, fin);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/medio-pago/{medioPago}")
    public ResponseEntity<List<VentaResponse>> listarPorMedioPago(@PathVariable Venta.MedioPago medioPago) {
        log.debug("Solicitud de ventas por medio de pago: {}", medioPago);
        List<VentaResponse> responses = ventaService.listarPorMedioPago(medioPago);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<VentaResponse>> listarPorEstado(@PathVariable Venta.EstadoVenta estado) {
        log.debug("Solicitud de ventas por estado: {}", estado);
        List<VentaResponse> responses = ventaService.listarPorEstado(estado);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/ultimas")
    public ResponseEntity<List<VentaResponse>> listarUltimasVentas(
            @RequestParam(defaultValue = "10") int limite) {
        log.debug("Solicitud de últimas {} ventas", limite);
        List<VentaResponse> responses = ventaService.listarUltimasVentas(limite);
        return ResponseEntity.ok(responses);
    }

    // ==============================
    // CONSULTAS DE AGREGACIÓN
    // ==============================

    @GetMapping("/total-periodo")
    public ResponseEntity<BigDecimal> obtenerTotalVentasPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.debug("Solicitud de total de ventas entre {} y {}", inicio, fin);
        BigDecimal total = ventaService.obtenerTotalVentasPorPeriodo(inicio, fin);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/total-medio-pago-periodo")
    public ResponseEntity<List<Object[]>> obtenerTotalVentasPorMedioPagoYPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.debug("Solicitud de total de ventas por medio de pago entre {} y {}", inicio, fin);
        List<Object[]> resultados = ventaService.obtenerTotalVentasPorMedioPagoYPeriodo(inicio, fin);
        return ResponseEntity.ok(resultados);
    }

    // ==============================
    // NUEVO ENDPOINT: ENVIAR BOLETA POR CORREO
    // ==============================

    @PostMapping("/{id}/enviar-boleta")
    public ResponseEntity<MensajeResponse> enviarBoletaPorCorreo(
            @PathVariable Integer id,
            @Valid @RequestBody CorreoRequest request) {
        log.debug("Solicitud de envío de boleta de venta ID: {} al correo: {}", id, request.getDestino());

        try {
            // 1. Obtener la venta con sus datos
            VentaResponse ventaResponse = ventaService.obtenerVentaPorId(id);

            // 2. Obtener los detalles de la venta
            List<DetalleVentaResponse> detalles = ventaService.obtenerDetallesVenta(id);

            // 3. Generar el PDF de la boleta
            byte[] pdfBytes = pdfGeneratorService.generarBoleta(ventaResponse, detalles);

            // 4. Construir el mensaje del correo
            String asunto = "Boleta de Venta N° " + id;
            String mensaje = String.format("""
                    Estimado cliente,

                    Adjunto encontrará la boleta de su compra.

                    N° Venta: %d
                    Fecha: %s
                    Total: S/ %.2f

                    ¡Gracias por su preferencia!
                    """,
                    id,
                    ventaResponse.getFecha() != null ? ventaResponse.getFecha().toString() : "N/A",
                    ventaResponse.getTotal() != null ? ventaResponse.getTotal() : BigDecimal.ZERO
            );

            // 5. Enviar el correo con el PDF adjunto
            emailService.enviarCorreoConAdjunto(
                    request.getDestino(),
                    asunto,
                    mensaje,
                    pdfBytes,
                    "boleta_" + id + ".pdf"
            );

            log.info("Boleta de venta ID {} enviada exitosamente a {}", id, request.getDestino());
            return ResponseEntity.ok(new MensajeResponse("Boleta enviada exitosamente al correo " + request.getDestino()));

        } catch (Exception e) {
            log.error("Error al enviar boleta de venta ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MensajeResponse("Error al enviar la boleta: " + e.getMessage()));
        }
    }
}