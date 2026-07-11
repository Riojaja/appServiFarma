package com.example.proyecto.app.controller;

import com.example.proyecto.app.dto.response.ProductoResponse;
import com.example.proyecto.app.entity.Lote;
import com.example.proyecto.app.service.InventarioService;
import com.example.proyecto.app.service.ProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Antes de este archivo, core/services/inventario.ts (frontend) llamaba a
 * /api/inventario/* pero este controlador no existía en el backend -> 404
 * en las 4 rutas. Este archivo las expone componiendo datos que ya existían
 * en ProductoService (stock bajo, sin stock) e InventarioService (lotes
 * próximos a vencer, lotes activos con stock), sin duplicar lógica de negocio.
 */
@Slf4j
@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final ProductoService productoService;
    private final InventarioService inventarioService;

    // ==============================
    // GET /api/inventario/stock-bajo
    // ==============================
    @GetMapping("/stock-bajo")
    public ResponseEntity<List<Map<String, Object>>> obtenerAlertasStockBajo() {
        log.debug("Solicitud de alertas de stock bajo");
        List<ProductoResponse> productos = productoService.obtenerProductosConStockBajo();

        List<Map<String, Object>> alertas = new ArrayList<>();
        for (ProductoResponse p : productos) {
            Map<String, Object> alerta = new HashMap<>();
            alerta.put("tipo", "stock_bajo");
            alerta.put("mensaje", "Stock bajo: " + p.getNombre());
            alerta.put("productoId", p.getId());
            alerta.put("productoNombre", p.getNombre());
            alertas.add(alerta);
        }
        return ResponseEntity.ok(alertas);
    }

    // ==============================
    // GET /api/inventario/proximos-vencer
    // ==============================
    @GetMapping("/proximos-vencer")
    public ResponseEntity<List<Map<String, Object>>> obtenerAlertasProximosVencer() {
        log.debug("Solicitud de alertas de lotes próximos a vencer");
        List<Lote> lotes = inventarioService.obtenerLotesProximosAVencer();

        List<Map<String, Object>> alertas = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        for (Lote lote : lotes) {
            long diasRestantes = ChronoUnit.DAYS.between(hoy, lote.getFechaVencimiento());
            Map<String, Object> alerta = new HashMap<>();
            alerta.put("tipo", "proximo_vencer");
            alerta.put("mensaje", "Próximo a vencer: " + lote.getProducto().getNombre() + " (lote " + lote.getLote() + ")");
            alerta.put("productoId", lote.getProducto().getId());
            alerta.put("productoNombre", lote.getProducto().getNombre());
            alerta.put("loteId", lote.getId());
            alerta.put("cantidad", lote.getCantidad());
            alerta.put("fechaVencimiento", lote.getFechaVencimiento().toString());
            alerta.put("diasRestantes", diasRestantes);
            alertas.add(alerta);
        }
        return ResponseEntity.ok(alertas);
    }

    // ==============================
    // GET /api/inventario/sin-stock
    // ==============================
    @GetMapping("/sin-stock")
    public ResponseEntity<List<ProductoResponse>> obtenerProductosSinStock() {
        log.debug("Solicitud de productos sin stock (vía /api/inventario)");
        return ResponseEntity.ok(productoService.obtenerProductosSinStock());
    }

    // ==============================
    // GET /api/inventario/resumen
    // ==============================
    @GetMapping("/resumen")
    public ResponseEntity<Map<String, Object>> obtenerResumen() {
        log.debug("Solicitud de resumen de inventario");

        int totalStockBajo = productoService.obtenerProductosConStockBajo().size();
        int totalSinStock = productoService.obtenerProductosSinStock().size();
        int totalProximosVencer = inventarioService.obtenerLotesProximosAVencer().size();
        int totalLotesActivos = inventarioService.obtenerLotesActivosConStock().size();

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("totalProductosStockBajo", totalStockBajo);
        resumen.put("totalProductosSinStock", totalSinStock);
        resumen.put("totalLotesProximosAVencer", totalProximosVencer);
        resumen.put("totalLotesActivosConStock", totalLotesActivos);

        return ResponseEntity.ok(resumen);
    }
}