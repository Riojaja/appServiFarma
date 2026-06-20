package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.dto.request.ProductoRequest;
import com.example.proyecto.app.dto.response.ProductoResponse;
import com.example.proyecto.app.entity.Categoria;
import com.example.proyecto.app.entity.Fabricante;
import com.example.proyecto.app.entity.Lote;
import com.example.proyecto.app.entity.Producto;
import com.example.proyecto.app.exception.BusinessException;
import com.example.proyecto.app.exception.DuplicadoException;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.mapper.ProductoMapper;
import com.example.proyecto.app.repository.CategoriaRepository;
import com.example.proyecto.app.repository.FabricanteRepository;
import com.example.proyecto.app.repository.LoteRepository;
import com.example.proyecto.app.repository.ProductoRepository;
import com.example.proyecto.app.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final FabricanteRepository fabricanteRepository;
    private final CategoriaRepository categoriaRepository;
    private final LoteRepository loteRepository;
    private final ProductoMapper productoMapper;

    // ==============================
    // OPERACIONES CRUD BÁSICAS
    // ==============================

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

        // 5. Mapear y guardar
        Producto producto = productoMapper.toEntity(request);
        producto.setCategoria(categoria);
        producto.setFabricante(fabricante);
        producto.setProductoGenerico(productoGenerico);

        Producto saved = productoRepository.save(producto);
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

        // 6. Actualizar datos (el mapper ignora los campos null para no sobreescribir con null)
        productoMapper.updateEntity(producto, request);
        producto.setCategoria(categoria);
        producto.setFabricante(fabricante);
        producto.setProductoGenerico(productoGenerico);

        Producto updated = productoRepository.save(producto);
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
        // 1. Verificar que el producto existe
        if (!productoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Producto con ID " + id + " no encontrado.");
        }

        // 2. Verificar que no tenga lotes asociados (integridad referencial)
        long lotesCount = loteRepository.countByProductoId(id);
        if (lotesCount > 0) {
            throw new BusinessException("No se puede eliminar el producto porque tiene " + lotesCount + " lotes asociados.");
        }

        productoRepository.deleteById(id);
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
        // Validar que la categoría existe (opcional pero recomendable)
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
        // Busca productos que tengan el mismo producto genérico o el mismo principio activo
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto con ID " + productoId + " no encontrado."));

        // Si el producto es genérico, buscar productos que lo tengan como genérico
        if (producto.getEsGenerico()) {
            return productoRepository.findByProductoGenericoId(productoId).stream()
                    .map(productoMapper::toResponse)
                    .collect(Collectors.toList());
        } else {
            // Si es comercial, buscar productos con el mismo principio activo (excluyendo el actual)
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
        // Verificar que el producto exista
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
}