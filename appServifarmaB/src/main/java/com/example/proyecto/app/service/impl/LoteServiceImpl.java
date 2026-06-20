package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.dto.request.LoteRequest;
import com.example.proyecto.app.dto.response.LoteResponse;
import com.example.proyecto.app.entity.Lote;
import com.example.proyecto.app.entity.Producto;
import com.example.proyecto.app.entity.Proveedor;
import com.example.proyecto.app.exception.DuplicadoException;
import com.example.proyecto.app.exception.ParametroInvalidoException;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.mapper.LoteMapper;
import com.example.proyecto.app.repository.LoteRepository;
import com.example.proyecto.app.repository.ProductoRepository;
import com.example.proyecto.app.repository.ProveedorRepository;
import com.example.proyecto.app.service.LoteService;
import com.example.proyecto.app.util.FechaUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoteServiceImpl implements LoteService {

    private final LoteRepository loteRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final LoteMapper loteMapper;

    // ==============================
    // OPERACIONES CRUD BÁSICAS
    // ==============================

    @Override
    @Transactional
    public LoteResponse crearLote(LoteRequest request) {
        // 1. Validar que el producto exista
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto con ID " + request.getProductoId() + " no encontrado."));

        // 2. Validar que el proveedor exista
        Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor con ID " + request.getProveedorId() + " no encontrado."));

        // 3. Validar que la fecha de vencimiento no sea anterior a la fecha de ingreso
        if (request.getFechaVencimiento().isBefore(request.getFechaIngreso())) {
            throw new ParametroInvalidoException("La fecha de vencimiento no puede ser anterior a la fecha de ingreso.");
        }

        // 4. Validar que la fecha de ingreso no sea futura
        if (!FechaUtils.esFechaValidaNoFutura(request.getFechaIngreso())) {
            throw new ParametroInvalidoException("La fecha de ingreso no puede ser una fecha futura.");
        }

        // 5. Verificar duplicado (mismo número de lote para el mismo producto)
        //    Nota: Si la lógica de negocio lo permite, puedes buscar por producto y lote.
        //    Por simplicidad, asumimos que el número de lote es único globalmente.
        //    Si no es así, puedes usar una consulta personalizada en el repositorio.
        //    Ejemplo: loteRepository.findByLoteAndProductoId(request.getLote(), producto.getId())
        if (loteRepository.findByLote(request.getLote()).isPresent()) {
            throw new DuplicadoException("Ya existe un lote con el número: " + request.getLote());
        }

        // 6. Mapear y guardar
        Lote lote = loteMapper.toEntity(request);
        lote.setProducto(producto);
        lote.setProveedor(proveedor);
        // El estado por defecto es 'activo' (definido en la entidad)

        Lote saved = loteRepository.save(lote);
        log.info("Lote creado: {} para producto: {}", saved.getLote(), producto.getNombre());
        return loteMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public LoteResponse actualizarLote(Integer id, LoteRequest request) {
        // 1. Verificar que el lote existe
        Lote lote = loteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lote con ID " + id + " no encontrado."));

        // 2. Validar que el producto exista (si se cambia)
        Producto producto = null;
        if (!lote.getProducto().getId().equals(request.getProductoId())) {
            producto = productoRepository.findById(request.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto con ID " + request.getProductoId() + " no encontrado."));
        } else {
            producto = lote.getProducto();
        }

        // 3. Validar que el proveedor exista (si se cambia)
        Proveedor proveedor = null;
        if (!lote.getProveedor().getId().equals(request.getProveedorId())) {
            proveedor = proveedorRepository.findById(request.getProveedorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor con ID " + request.getProveedorId() + " no encontrado."));
        } else {
            proveedor = lote.getProveedor();
        }

        // 4. Validar fechas
        if (request.getFechaVencimiento().isBefore(request.getFechaIngreso())) {
            throw new ParametroInvalidoException("La fecha de vencimiento no puede ser anterior a la fecha de ingreso.");
        }
        if (!FechaUtils.esFechaValidaNoFutura(request.getFechaIngreso())) {
            throw new ParametroInvalidoException("La fecha de ingreso no puede ser una fecha futura.");
        }

        // 5. Verificar duplicado de número de lote (si cambia y no es el mismo)
        if (!lote.getLote().equals(request.getLote()) && loteRepository.findByLote(request.getLote()).isPresent()) {
            throw new DuplicadoException("Ya existe un lote con el número: " + request.getLote());
        }

        // 6. Actualizar datos (usando el mapper)
        loteMapper.updateEntity(lote, request);
        lote.setProducto(producto);
        lote.setProveedor(proveedor);

        Lote updated = loteRepository.save(lote);
        log.info("Lote actualizado: {}", updated.getLote());
        return loteMapper.toResponse(updated);
    }

    @Override
    public LoteResponse obtenerLotePorId(Integer id) {
        Lote lote = loteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lote con ID " + id + " no encontrado."));
        return loteMapper.toResponse(lote);
    }

    @Override
    public List<LoteResponse> listarTodos() {
        return loteRepository.findAll().stream()
                .map(loteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminarLote(Integer id) {
        Lote lote = loteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lote con ID " + id + " no encontrado."));

        // Si quieres mantener integridad, puedes verificar si tiene movimientos asociados
        // (la BD ya tiene RESTRICT, pero puedes agregar validación adicional aquí)

        loteRepository.deleteById(id);
        log.info("Lote eliminado: {} - Número: {}", id, lote.getLote());
    }

    // ==============================
    // CONSULTAS ESPECÍFICAS
    // ==============================

    @Override
    public List<LoteResponse> listarPorProducto(Integer productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new ResourceNotFoundException("Producto con ID " + productoId + " no encontrado.");
        }
        return loteRepository.findByProductoIdOrderByFechaIngresoDesc(productoId).stream()
                .map(loteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoteResponse> listarPorEstado(Lote.EstadoLote estado) {
        return loteRepository.findByEstado(estado).stream()
                .map(loteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LoteResponse buscarPorLote(String numeroLote) {
        Lote lote = loteRepository.findByLote(numeroLote)
                .orElseThrow(() -> new ResourceNotFoundException("Lote con número " + numeroLote + " no encontrado."));
        return loteMapper.toResponse(lote);
    }

    @Override
    public List<LoteResponse> buscarPorLoteContaining(String numeroLote) {
        return loteRepository.findByLoteContaining(numeroLote).stream()
                .map(loteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoteResponse> obtenerLotesProximosAVencer(int diasAnticipacion) {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.plusDays(diasAnticipacion);
        return loteRepository.findByFechaVencimientoBetweenAndEstado(hoy, fechaLimite, Lote.EstadoLote.activo).stream()
                .map(loteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoteResponse> obtenerLotesVencidos() {
        LocalDate hoy = LocalDate.now();
        // Buscamos lotes activos con fecha de vencimiento anterior a hoy
        // (los que deberían estar vencidos pero aún no se actualizaron)
        List<Lote> vencidos = loteRepository.findByFechaVencimientoBeforeAndEstadoNot(hoy, Lote.EstadoLote.vencido);
        return vencidos.stream()
                .map(loteMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==============================
    // OPERACIONES DE ACTUALIZACIÓN DE ESTADO
    // ==============================

    @Override
    @Transactional
    public void marcarComoDeteriorado(Integer id) {
        Lote lote = loteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lote con ID " + id + " no encontrado."));
        lote.setEstado(Lote.EstadoLote.deteriorado);
        loteRepository.save(lote);
        log.info("Lote {} marcado como deteriorado", id);
    }

    @Override
    @Transactional
    public int actualizarLotesVencidos() {
        // Este método usa el método masivo del repositorio
        return loteRepository.marcarLotesVencidos(LocalDate.now(), Lote.EstadoLote.vencido);
    }
}