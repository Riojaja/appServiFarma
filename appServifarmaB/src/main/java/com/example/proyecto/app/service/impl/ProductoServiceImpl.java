package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.dto.request.ProductoRequest;
import com.example.proyecto.app.dto.response.ProductoResponse;
import com.example.proyecto.app.entity.*;
import com.example.proyecto.app.exception.BusinessException;
import com.example.proyecto.app.exception.DuplicadoException;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.mapper.ProductoMapper;
import com.example.proyecto.app.repository.CategoriaRepository;
import com.example.proyecto.app.repository.FabricanteRepository;
import com.example.proyecto.app.repository.LoteRepository;
import com.example.proyecto.app.repository.ProductoRepository;
import com.example.proyecto.app.service.BitacoraComunicacionService;
import com.example.proyecto.app.service.ProductoService;
import com.example.proyecto.app.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductoServiceImpl implements ProductoService {

    private static final Logger log = LoggerFactory.getLogger(ProductoServiceImpl.class);

    private final ProductoRepository productoRepository;
    private final FabricanteRepository fabricanteRepository;
    private final CategoriaRepository categoriaRepository;
    private final LoteRepository loteRepository;
    private final ProductoMapper productoMapper;
    private final BitacoraComunicacionService bitacoraService;
    private final SecurityUtils securityUtils;
    private final ProductoImportService productoImportService;

    // ==============================
    // OPERACIONES CRUD BÁSICAS
    // ==============================

    @Override
    @Transactional
    public void actualizarImagenProducto(Integer id, String rutaImagen) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        producto.setImagen(rutaImagen);
        productoRepository.save(producto);
        log.info("Imagen actualizada para producto ID: {}", id);
    }

    @Override
    @Transactional
    public ProductoResponse crearProducto(ProductoRequest request) {
        // 1. Validar código de barras único
        if (productoRepository.existsByCodigoBarras(request.getCodigoBarras())) {
            throw new DuplicadoException("Ya existe un producto con el código de barras: " + request.getCodigoBarras());
        }

        // 2. Validar que la categoría exista (si se proporciona)
        Categoria categoria = null;
        if (request.getCategoriaId() != null) {
            categoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría con ID " + request.getCategoriaId() + " no encontrada."));
        }

        // 3. Validar que el fabricante exista (si se proporciona)
        Fabricante fabricante = null;
        if (request.getFabricanteId() != null) {
            fabricante = fabricanteRepository.findById(request.getFabricanteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fabricante con ID " + request.getFabricanteId() + " no encontrado."));
        }

        // 4. Validar que el producto genérico exista (si se proporciona)
        Producto productoGenerico = null;
        if (request.getProductoGenericoId() != null) {
            productoGenerico = productoRepository.findById(request.getProductoGenericoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto genérico con ID " + request.getProductoGenericoId() + " no encontrado."));
        }

        // 5. Mapear a entidad (sin procesar imagen aún)
        Producto producto = productoMapper.toEntity(request);
        producto.setCategoria(categoria);
        producto.setFabricante(fabricante);
        producto.setProductoGenerico(productoGenerico);

        // ✅ Guardar primero el producto para obtener un ID real
        Producto saved = productoRepository.save(producto);
        log.info("Producto creado (sin imagen aún): {} (ID: {})", saved.getNombre(), saved.getId());

        // 6. Procesar imagen DESPUÉS de tener el ID
        String imagenOriginal = request.getImagen();

        // Caso 1: La imagen es una URL externa → descargar y guardar localmente
        if (imagenOriginal != null && !imagenOriginal.isEmpty()
                && (imagenOriginal.startsWith("http://") || imagenOriginal.startsWith("https://"))) {
            try {
                String rutaLocal = productoImportService.guardarImagenDesdeUrl(imagenOriginal, saved.getId());
                saved.setImagen(rutaLocal);
                productoRepository.save(saved);
                log.info("Imagen descargada desde URL y guardada para producto ID: {}", saved.getId());
            } catch (Exception e) {
                log.warn("No se pudo descargar la imagen desde URL para producto ID {}: {}", saved.getId(), e.getMessage());
                // No guardamos la URL original como imagen (dejamos null)
            }
        }
        // Caso 2: La imagen es una ruta local (ya subida por el endpoint /imagen) → no hacer nada, ya está guardada
        // Caso 3: No hay imagen → se queda null

        // 7. Actualizar la entidad si se cambió la imagen
        if (saved.getImagen() != null) {
            // Ya actualizado
        }

        // ==============================================================
        // CREAR MENSAJE EN BITÁCORA
        // ==============================================================
        try {
            String categoriaNombre = categoria != null ? categoria.getNombre() : "Sin categoría";
            String fabricanteNombre = fabricante != null ? fabricante.getNombre() : "Sin fabricante";

            String mensaje = String.format(
                    "📦 Nuevo producto registrado: %s - Categoría: %s - Fabricante: %s - Precio: S/ %.2f",
                    saved.getNombre(),
                    categoriaNombre,
                    fabricanteNombre,
                    saved.getPrecioVentaActual()
            );

            Integer usuarioId = getUsuarioId();

            com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest =
                    com.example.proyecto.app.dto.request.BitacoraComunicacionRequest.builder()
                            .usuarioId(usuarioId)
                            .mensaje(mensaje)
                            .tipo(BitacoraComunicacion.Tipo.novedad)
                            .build();

            bitacoraService.crearMensaje(bitacoraRequest);
            log.info("Mensaje de bitácora creado para nuevo producto ID: {}", saved.getId());
        } catch (Exception e) {
            log.error("Error al crear mensaje en bitácora para nuevo producto: {}", e.getMessage());
        }

        return productoMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProductoResponse actualizarProducto(Integer id, ProductoRequest request) {
        // 1. Verificar que el producto existe
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto con ID " + id + " no encontrado."));

        // 2. Validar código de barras único (si cambia)
        if (!producto.getCodigoBarras().equals(request.getCodigoBarras()) &&
                productoRepository.existsByCodigoBarras(request.getCodigoBarras())) {
            throw new DuplicadoException("Ya existe otro producto con el código de barras: " + request.getCodigoBarras());
        }

        // 3. Validar la categoría
        Categoria categoria = null;
        if (request.getCategoriaId() != null) {
            categoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría con ID " + request.getCategoriaId() + " no encontrada."));
        }

        // 4. Validar el fabricante
        Fabricante fabricante = null;
        if (request.getFabricanteId() != null) {
            fabricante = fabricanteRepository.findById(request.getFabricanteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fabricante con ID " + request.getFabricanteId() + " no encontrado."));
        }

        // 5. Validar el producto genérico
        Producto productoGenerico = null;
        if (request.getProductoGenericoId() != null) {
            productoGenerico = productoRepository.findById(request.getProductoGenericoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto genérico con ID " + request.getProductoGenericoId() + " no encontrado."));
        }

        // 6. Actualizar datos básicos (sin imagen)
        productoMapper.updateEntity(producto, request);
        producto.setCategoria(categoria);
        producto.setFabricante(fabricante);
        producto.setProductoGenerico(productoGenerico);

        // 7. Procesar imagen DESPUÉS de actualizar los datos
        String imagenOriginal = request.getImagen();

        // Caso 1: La imagen es una URL externa → descargar y guardar localmente
        if (imagenOriginal != null && !imagenOriginal.isEmpty()
                && (imagenOriginal.startsWith("http://") || imagenOriginal.startsWith("https://"))) {
            try {
                String rutaLocal = productoImportService.guardarImagenDesdeUrl(imagenOriginal, id);
                producto.setImagen(rutaLocal);
                log.info("Imagen descargada desde URL para actualización de producto ID: {}", id);
            } catch (Exception e) {
                log.warn("No se pudo descargar la imagen desde URL para actualización de producto ID {}: {}", id, e.getMessage());
                // No guardamos la URL original como imagen (dejamos null)
            }
        }
        // Caso 2: La imagen es una ruta local (ya subida por el endpoint /imagen) → no hacer nada
        // Caso 3: No hay imagen → se mantiene la anterior o se limpia si se envió null

        // Guardar los cambios
        Producto updated = productoRepository.save(producto);
        log.info("Producto actualizado: {} (ID: {})", updated.getNombre(), updated.getId());

        // ==============================================================
        // CREAR MENSAJE EN BITÁCORA
        // ==============================================================
        try {
            Integer usuarioId = getUsuarioId();
            String mensaje = String.format(
                    "✏️ Producto actualizado: %s (ID: %d) - Nuevo precio: S/ %.2f",
                    updated.getNombre(),
                    updated.getId(),
                    updated.getPrecioVentaActual()
            );

            com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest =
                    com.example.proyecto.app.dto.request.BitacoraComunicacionRequest.builder()
                            .usuarioId(usuarioId)
                            .mensaje(mensaje)
                            .tipo(BitacoraComunicacion.Tipo.novedad)
                            .build();

            bitacoraService.crearMensaje(bitacoraRequest);
            log.info("Mensaje de bitácora creado para actualización de producto ID: {}", id);
        } catch (Exception e) {
            log.error("Error al crear mensaje en bitácora para actualización de producto: {}", e.getMessage());
        }

        return productoMapper.toResponse(updated);
    }

    @Override
    public ProductoResponse obtenerProductoPorId(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto con ID " + id + " no encontrado."));
        return productoMapper.toResponse(producto);
    }

    @Override
    public List<ProductoResponse> listarTodos() {
        return productoRepository.findAllByOrderByNombreAsc().stream()
                .map(productoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminarProducto(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto con ID " + id + " no encontrado."));

        long lotesCount = loteRepository.countByProductoId(id);
        if (lotesCount > 0) {
            throw new BusinessException("No se puede eliminar el producto porque tiene " + lotesCount + " lotes asociados.");
        }

        String nombreProducto = producto.getNombre();
        productoRepository.deleteById(id);
        log.info("Producto eliminado: {} (ID: {})", nombreProducto, id);

        try {
            Integer usuarioId = getUsuarioId();
            String mensaje = String.format(
                    "🗑️ Producto eliminado: %s (ID: %d)",
                    nombreProducto,
                    id
            );

            com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest =
                    com.example.proyecto.app.dto.request.BitacoraComunicacionRequest.builder()
                            .usuarioId(usuarioId)
                            .mensaje(mensaje)
                            .tipo(BitacoraComunicacion.Tipo.incidencia)
                            .build();

            bitacoraService.crearMensaje(bitacoraRequest);
            log.info("Mensaje de bitácora creado para eliminación de producto ID: {}", id);
        } catch (Exception e) {
            log.error("Error al crear mensaje en bitácora para eliminación de producto: {}", e.getMessage());
        }
    }

    // ==============================
    // BÚSQUEDAS POR CAMPOS ESPECÍFICOS
    // ==============================

    @Override
    public ProductoResponse buscarPorCodigoBarras(String codigoBarras) {
        Producto producto = productoRepository.findByCodigoBarras(codigoBarras)
                .orElseThrow(() -> new ResourceNotFoundException("Producto con código de barras " + codigoBarras + " no encontrado."));
        return productoMapper.toResponse(producto);
    }

    @Override
    public List<ProductoResponse> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(productoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoResponse> buscarPorPrincipioActivo(String principioActivo) {
        return productoRepository.findByPrincipioActivoContainingIgnoreCase(principioActivo).stream()
                .map(productoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoResponse> buscarPorNombreOCodigo(String texto) {
        return productoRepository.findByNombreContainingIgnoreCaseOrCodigoBarrasContaining(texto, texto).stream()
                .map(productoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoResponse> buscarPorCategoria(Integer categoriaId) {
        if (!categoriaRepository.existsById(categoriaId)) {
            throw new ResourceNotFoundException("Categoría con ID " + categoriaId + " no encontrada.");
        }
        return productoRepository.findByCategoriaId(categoriaId).stream()
                .map(productoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoResponse> buscarPorFabricante(Integer fabricanteId) {
        if (!fabricanteRepository.existsById(fabricanteId)) {
            throw new ResourceNotFoundException("Fabricante con ID " + fabricanteId + " no encontrado.");
        }
        return productoRepository.findByFabricanteId(fabricanteId).stream()
                .map(productoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoResponse> listarGenericos() {
        return productoRepository.findByEsGenerico(true).stream()
                .map(productoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoResponse> buscarAlternativasGenericas(Integer productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto con ID " + productoId + " no encontrado."));

        if (producto.getEsGenerico()) {
            return productoRepository.findByProductoGenericoId(productoId).stream()
                    .map(productoMapper::toResponse)
                    .collect(Collectors.toList());
        } else {
            return productoRepository.findByPrincipioActivoContainingIgnoreCase(producto.getPrincipioActivo()).stream()
                    .filter(p -> !p.getId().equals(productoId))
                    .map(productoMapper::toResponse)
                    .collect(Collectors.toList());
        }
    }

    // ==============================
    // CONSULTAS DE STOCK Y ALERTAS
    // ==============================

    @Override
    public Integer obtenerStockActual(Integer productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new ResourceNotFoundException("Producto con ID " + productoId + " no encontrado.");
        }
        return loteRepository.sumCantidadByProductoIdAndEstado(productoId, Lote.EstadoLote.activo);
    }

    @Override
    public List<ProductoResponse> obtenerProductosConStockBajo() {
        return productoRepository.findProductosConStockBajo().stream()
                .map(productoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoResponse> obtenerProductosSinStock() {
        return productoRepository.findProductosSinStock().stream()
                .map(productoMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==============================
    // VALIDACIONES
    // ==============================

    @Override
    public boolean existePorCodigoBarras(String codigoBarras) {
        return productoRepository.existsByCodigoBarras(codigoBarras);
    }

    // ==============================
    // MÉTODO AUXILIAR PARA OBTENER USUARIO
    // ==============================

    private Integer getUsuarioId() {
        try {
            return securityUtils.getUsuarioAutenticado().getId();
        } catch (Exception e) {
            log.debug("No se pudo obtener usuario autenticado, usando usuario sistema (ID 1)");
            return 1;
        }
    }
}