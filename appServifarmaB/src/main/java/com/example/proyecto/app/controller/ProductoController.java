package com.example.proyecto.app.controller;

import com.example.proyecto.app.dto.request.ProductoRequest;
import com.example.proyecto.app.dto.response.MensajeResponse;
import com.example.proyecto.app.dto.response.ProductoResponse;
import com.example.proyecto.app.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    // ==============================
    // OPERACIONES CRUD BÁSICAS
    // ==============================

    @PostMapping
    public ResponseEntity<ProductoResponse> crearProducto(@Valid @RequestBody ProductoRequest request) {
        log.debug("Solicitud de creación de producto: {}", request.getNombre());
        ProductoResponse response = productoService.crearProducto(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizarProducto(
            @PathVariable Integer id,
            @Valid @RequestBody ProductoRequest request) {
        log.debug("Solicitud de actualización de producto con ID: {}", id);
        ProductoResponse response = productoService.actualizarProducto(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtenerProductoPorId(@PathVariable Integer id) {
        log.debug("Solicitud de producto con ID: {}", id);
        ProductoResponse response = productoService.obtenerProductoPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listarTodos() {
        log.debug("Solicitud de listado de todos los productos");
        List<ProductoResponse> responses = productoService.listarTodos();
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminarProducto(@PathVariable Integer id) {
        log.debug("Solicitud de eliminación de producto con ID: {}", id);
        productoService.eliminarProducto(id);
        return ResponseEntity.ok(new MensajeResponse("Producto eliminado correctamente"));
    }

    // ==============================
    // BÚSQUEDAS POR CAMPOS ESPECÍFICOS
    // ==============================

    @GetMapping("/codigo-barras/{codigo}")
    public ResponseEntity<ProductoResponse> buscarPorCodigoBarras(@PathVariable String codigo) {
        log.debug("Solicitud de producto por código de barras: {}", codigo);
        ProductoResponse response = productoService.buscarPorCodigoBarras(codigo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoResponse>> buscarPorNombre(@RequestParam String nombre) {
        log.debug("Solicitud de búsqueda de productos por nombre: {}", nombre);
        List<ProductoResponse> responses = productoService.buscarPorNombre(nombre);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/buscar/principio-activo")
    public ResponseEntity<List<ProductoResponse>> buscarPorPrincipioActivo(@RequestParam String principioActivo) {
        log.debug("Solicitud de búsqueda de productos por principio activo: {}", principioActivo);
        List<ProductoResponse> responses = productoService.buscarPorPrincipioActivo(principioActivo);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/buscar/texto")
    public ResponseEntity<List<ProductoResponse>> buscarPorNombreOCodigo(@RequestParam String texto) {
        log.debug("Solicitud de búsqueda de productos por texto (nombre o código): {}", texto);
        List<ProductoResponse> responses = productoService.buscarPorNombreOCodigo(texto);
        return ResponseEntity.ok(responses);
    }

    // ==============================
    // FILTROS POR RELACIONES
    // ==============================

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ProductoResponse>> buscarPorCategoria(@PathVariable Integer categoriaId) {
        log.debug("Solicitud de productos por categoría ID: {}", categoriaId);
        List<ProductoResponse> responses = productoService.buscarPorCategoria(categoriaId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/fabricante/{fabricanteId}")
    public ResponseEntity<List<ProductoResponse>> buscarPorFabricante(@PathVariable Integer fabricanteId) {
        log.debug("Solicitud de productos por fabricante ID: {}", fabricanteId);
        List<ProductoResponse> responses = productoService.buscarPorFabricante(fabricanteId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/genericos")
    public ResponseEntity<List<ProductoResponse>> listarGenericos() {
        log.debug("Solicitud de listado de productos genéricos");
        List<ProductoResponse> responses = productoService.listarGenericos();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}/alternativas")
    public ResponseEntity<List<ProductoResponse>> buscarAlternativasGenericas(@PathVariable Integer id) {
        log.debug("Solicitud de alternativas genéricas para el producto ID: {}", id);
        List<ProductoResponse> responses = productoService.buscarAlternativasGenericas(id);
        return ResponseEntity.ok(responses);
    }

    // ==============================
    // CONSULTAS DE STOCK Y ALERTAS
    // ==============================

    @GetMapping("/{id}/stock")
    public ResponseEntity<Integer> obtenerStockActual(@PathVariable Integer id) {
        log.debug("Solicitud de stock actual para producto ID: {}", id);
        Integer stock = productoService.obtenerStockActual(id);
        return ResponseEntity.ok(stock);
    }

    @GetMapping("/alertas/stock-bajo")
    public ResponseEntity<List<ProductoResponse>> obtenerProductosConStockBajo() {
        log.debug("Solicitud de productos con stock bajo");
        List<ProductoResponse> responses = productoService.obtenerProductosConStockBajo();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/alertas/sin-stock")
    public ResponseEntity<List<ProductoResponse>> obtenerProductosSinStock() {
        log.debug("Solicitud de productos sin stock");
        List<ProductoResponse> responses = productoService.obtenerProductosSinStock();
        return ResponseEntity.ok(responses);
    }

    // ==============================
    // VALIDACIONES
    // ==============================

    @GetMapping("/existe")
    public ResponseEntity<Boolean> existePorCodigoBarras(@RequestParam String codigo) {
        log.debug("Solicitud de verificación de existencia de producto con código de barras: {}", codigo);
        boolean existe = productoService.existePorCodigoBarras(codigo);
        return ResponseEntity.ok(existe);
    }
}