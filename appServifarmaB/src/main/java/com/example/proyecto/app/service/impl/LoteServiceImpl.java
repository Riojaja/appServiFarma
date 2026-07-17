package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.dto.request.LoteRequest;
import com.example.proyecto.app.dto.response.LoteResponse;
import com.example.proyecto.app.entity.BitacoraComunicacion;
import com.example.proyecto.app.entity.Lote;
import com.example.proyecto.app.entity.Producto;
import com.example.proyecto.app.entity.Proveedor;
import com.example.proyecto.app.entity.Usuario;
import com.example.proyecto.app.exception.DuplicadoException;
import com.example.proyecto.app.exception.ParametroInvalidoException;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.mapper.LoteMapper;
import com.example.proyecto.app.repository.LoteRepository;
import com.example.proyecto.app.repository.ProductoRepository;
import com.example.proyecto.app.repository.ProveedorRepository;
import com.example.proyecto.app.service.BitacoraComunicacionService;
import com.example.proyecto.app.service.LoteService;
import com.example.proyecto.app.util.FechaUtils;
import com.example.proyecto.app.util.SecurityUtils;
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
	private final com.example.proyecto.app.repository.UsuarioRepository usuarioRepository;
	private final com.example.proyecto.app.repository.MovimientoStockRepository movimientoStockRepository;
	private final BitacoraComunicacionService bitacoraService;
	private final SecurityUtils securityUtils;

	// ==============================
	// OPERACIONES CRUD BÁSICAS
	// ==============================

	@Override
	@Transactional
	public LoteResponse crearLote(LoteRequest request) {
		// 1. Validar que el producto exista
		Producto producto = productoRepository.findById(request.getProductoId()).orElseThrow(
				() -> new ResourceNotFoundException("Producto con ID " + request.getProductoId() + " no encontrado."));

		// 2. Validar que el proveedor exista
		Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Proveedor con ID " + request.getProveedorId() + " no encontrado."));

		// 3. Validar que el usuario exista
		Usuario usuario = usuarioRepository.findById(request.getUsuarioId()).orElseThrow(
				() -> new ResourceNotFoundException("Usuario con ID " + request.getUsuarioId() + " no encontrado."));

		// 4. Validar fechas
		if (request.getFechaVencimiento().isBefore(request.getFechaIngreso())) {
			throw new ParametroInvalidoException(
					"La fecha de vencimiento no puede ser anterior a la fecha de ingreso.");
		}
		if (!FechaUtils.esFechaValidaNoFutura(request.getFechaIngreso())) {
			throw new ParametroInvalidoException("La fecha de ingreso no puede ser una fecha futura.");
		}
		if (!request.getFechaVencimiento().isAfter(LocalDate.now())) {
			throw new ParametroInvalidoException("La fecha de vencimiento debe ser una fecha futura.");
		}

		// 5. Verificar duplicado
		if (loteRepository.findByLote(request.getLote()).isPresent()) {
			throw new DuplicadoException("Ya existe un lote con el número: " + request.getLote());
		}

		// 6. Mapear y guardar lote
		Lote lote = loteMapper.toEntity(request);
		lote.setProducto(producto);
		lote.setProveedor(proveedor);
		Lote saved = loteRepository.save(lote);
		log.info("Lote creado: {} para producto: {}", saved.getLote(), producto.getNombre());

		// ==============================================================
		// 7. CREAR MOVIMIENTO DE STOCK (COMPRA)
		// ==============================================================
		com.example.proyecto.app.entity.MovimientoStock movimiento = com.example.proyecto.app.entity.MovimientoStock
				.builder().lote(saved).usuario(usuario)
				.tipoMovimiento(com.example.proyecto.app.entity.MovimientoStock.TipoMovimiento.compra)
				.cantidad(saved.getCantidad()).costoUnitario(saved.getPrecioCompra())
				.observacion("Ingreso inicial de lote").build();

		movimientoStockRepository.save(movimiento);
		log.info("Movimiento de compra registrado para el lote ID: {}", saved.getId());

		// ==============================================================
		// 8. CREAR MENSAJE EN BITÁCORA
		// ==============================================================
		try {
			String mensaje = String.format(
					"📦 Nuevo lote registrado: %s (Lote %s) - Stock: %d unidades - Proveedor: %s", producto.getNombre(),
					saved.getLote(), saved.getCantidad(), proveedor.getRazonSocial());

			com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest = com.example.proyecto.app.dto.request.BitacoraComunicacionRequest
					.builder().usuarioId(usuario.getId()).mensaje(mensaje).tipo(BitacoraComunicacion.Tipo.novedad)
					.build();

			bitacoraService.crearMensaje(bitacoraRequest);
			log.info("Mensaje de bitácora creado para nuevo lote ID: {}", saved.getId());
		} catch (Exception e) {
			log.error("Error al crear mensaje en bitácora para nuevo lote: {}", e.getMessage());
			// No propagamos la excepción para no interrumpir el flujo principal
		}

		return loteMapper.toResponse(saved);
	}

	@Override
	@Transactional
	public LoteResponse actualizarLote(Integer id, LoteRequest request) {
		// 1. Verificar que el lote existe
		Lote lote = loteRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Lote con ID " + id + " no encontrado."));

		// 2. Validar que el producto exista (si se cambia)
		Producto producto;
		if (!lote.getProducto().getId().equals(request.getProductoId())) {
			producto = productoRepository.findById(request.getProductoId())
					.orElseThrow(() -> new ResourceNotFoundException(
							"Producto con ID " + request.getProductoId() + " no encontrado."));
		} else {
			producto = lote.getProducto();
		}

		// 3. Validar que el proveedor exista (si se cambia)
		Proveedor proveedor;
		if (!lote.getProveedor().getId().equals(request.getProveedorId())) {
			proveedor = proveedorRepository.findById(request.getProveedorId())
					.orElseThrow(() -> new ResourceNotFoundException(
							"Proveedor con ID " + request.getProveedorId() + " no encontrado."));
		} else {
			proveedor = lote.getProveedor();
		}

		// 4. Validar fechas
		if (request.getFechaVencimiento().isBefore(request.getFechaIngreso())) {
			throw new ParametroInvalidoException(
					"La fecha de vencimiento no puede ser anterior a la fecha de ingreso.");
		}
		if (!FechaUtils.esFechaValidaNoFutura(request.getFechaIngreso())) {
			throw new ParametroInvalidoException("La fecha de ingreso no puede ser una fecha futura.");
		}

		// 5. Verificar duplicado de número de lote
		if (!lote.getLote().equals(request.getLote()) && loteRepository.findByLote(request.getLote()).isPresent()) {
			throw new DuplicadoException("Ya existe un lote con el número: " + request.getLote());
		}

		// 6. Actualizar datos
		loteMapper.updateEntity(lote, request);
		lote.setProducto(producto);
		lote.setProveedor(proveedor);

		Lote updated = loteRepository.save(lote);
		log.info("Lote actualizado: {}", updated.getLote());

		// ==============================================================
		// 7. CREAR MENSAJE EN BITÁCORA
		// ==============================================================
		try {
			Usuario usuario = securityUtils.getUsuarioAutenticado();

			String mensaje = String.format("✏️ Lote actualizado: %s (Lote %s) - Nuevo stock: %d unidades",
					producto.getNombre(), updated.getLote(), updated.getCantidad());

			com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest = com.example.proyecto.app.dto.request.BitacoraComunicacionRequest
					.builder().usuarioId(usuario.getId()).mensaje(mensaje).tipo(BitacoraComunicacion.Tipo.novedad)
					.build();

			bitacoraService.crearMensaje(bitacoraRequest);
			log.info("Mensaje de bitácora creado para actualización de lote ID: {}", id);
		} catch (Exception e) {
			log.error("Error al crear mensaje en bitácora para actualización de lote: {}", e.getMessage());
		}

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
		return loteRepository.findAll().stream().map(loteMapper::toResponse).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void eliminarLote(Integer id) {
		// 1. Obtener el lote
		Lote lote = loteRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Lote con ID " + id + " no encontrado."));

		String nombreProducto = lote.getProducto().getNombre();
		String numeroLote = lote.getLote();
		Integer cantidad = lote.getCantidad();

		// 2. Intentar eliminar, capturando excepción de integridad referencial
		try {
			loteRepository.deleteById(id);
			log.info("Lote eliminado: {} (ID: {})", numeroLote, id);
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			// Lanzar BusinessException con mensaje claro
			throw new com.example.proyecto.app.exception.BusinessException("No se puede eliminar el lote '" + numeroLote
					+ "' porque tiene movimientos de stock o ventas asociadas.");
		}

		// 3. Crear mensaje en bitácora (solo si se eliminó correctamente)
		try {
			Usuario usuario = securityUtils.getUsuarioAutenticado();

			String mensaje = String.format("🗑️ Lote eliminado: %s (Lote %s) - ID: %d - Stock: %d unidades",
					nombreProducto, numeroLote, id, cantidad);

			com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest = com.example.proyecto.app.dto.request.BitacoraComunicacionRequest
					.builder().usuarioId(usuario.getId()).mensaje(mensaje).tipo(BitacoraComunicacion.Tipo.incidencia)
					.build();

			bitacoraService.crearMensaje(bitacoraRequest);
			log.info("Mensaje de bitácora creado para eliminación de lote ID: {}", id);
		} catch (Exception e) {
			log.error("Error al crear mensaje en bitácora para eliminación de lote: {}", e.getMessage());
		}
	}
	// ==============================
	// CONSULTAS ESPECÍFICAS
	// ==============================

	@Override
	public List<LoteResponse> listarPorProducto(Integer productoId) {
		if (!productoRepository.existsById(productoId)) {
			throw new ResourceNotFoundException("Producto con ID " + productoId + " no encontrado.");
		}
		return loteRepository.findByProductoIdOrderByFechaIngresoDesc(productoId).stream().map(loteMapper::toResponse)
				.collect(Collectors.toList());
	}

	@Override
	public List<LoteResponse> listarPorEstado(Lote.EstadoLote estado) {
		return loteRepository.findByEstado(estado).stream().map(loteMapper::toResponse).collect(Collectors.toList());
	}

	@Override
	public LoteResponse buscarPorLote(String numeroLote) {
		Lote lote = loteRepository.findByLote(numeroLote)
				.orElseThrow(() -> new ResourceNotFoundException("Lote con número " + numeroLote + " no encontrado."));
		return loteMapper.toResponse(lote);
	}

	@Override
	public List<LoteResponse> buscarPorLoteContaining(String numeroLote) {
		return loteRepository.findByLoteContaining(numeroLote).stream().map(loteMapper::toResponse)
				.collect(Collectors.toList());
	}

	@Override
	public List<LoteResponse> obtenerLotesProximosAVencer(int diasAnticipacion) {
		LocalDate hoy = LocalDate.now();
		LocalDate fechaLimite = hoy.plusDays(diasAnticipacion);
		return loteRepository.findByFechaVencimientoBetweenAndEstado(hoy, fechaLimite, Lote.EstadoLote.activo).stream()
				.map(loteMapper::toResponse).collect(Collectors.toList());
	}

	@Override
	public List<LoteResponse> obtenerLotesVencidos() {
		LocalDate hoy = LocalDate.now();
		List<Lote> vencidos = loteRepository.findByFechaVencimientoBeforeAndEstadoNot(hoy, Lote.EstadoLote.vencido);
		return vencidos.stream().map(loteMapper::toResponse).collect(Collectors.toList());
	}

	// ==============================
	// OPERACIONES DE ACTUALIZACIÓN DE ESTADO
	// ==============================

	@Override
	@Transactional
	public void marcarComoDeteriorado(Integer id) {
		Lote lote = loteRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Lote con ID " + id + " no encontrado."));

		String nombreProducto = lote.getProducto().getNombre();
		String numeroLote = lote.getLote();
		Integer cantidad = lote.getCantidad();

		lote.setEstado(Lote.EstadoLote.deteriorado);
		loteRepository.save(lote);
		log.info("Lote {} marcado como deteriorado", id);

		// ==============================================================
		// CREAR MENSAJE EN BITÁCORA
		// ==============================================================
		try {
			Usuario usuario = securityUtils.getUsuarioAutenticado();

			String mensaje = String.format("⚠️ Lote marcado como DETERIORADO: %s (Lote %s) - Cantidad: %d unidades",
					nombreProducto, numeroLote, cantidad);

			com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest = com.example.proyecto.app.dto.request.BitacoraComunicacionRequest
					.builder().usuarioId(usuario.getId()).mensaje(mensaje).tipo(BitacoraComunicacion.Tipo.incidencia)
					.build();

			bitacoraService.crearMensaje(bitacoraRequest);
			log.info("Mensaje de bitácora creado para lote deteriorado ID: {}", id);
		} catch (Exception e) {
			log.error("Error al crear mensaje en bitácora para lote deteriorado: {}", e.getMessage());
		}
	}

	@Override
	@Transactional
	public int actualizarLotesVencidos() {
		int actualizados = loteRepository.marcarLotesVencidos(LocalDate.now(), Lote.EstadoLote.vencido);

		// ==============================================================
		// CREAR MENSAJE EN BITÁCORA
		// ==============================================================
		if (actualizados > 0) {
			try {
				Usuario usuario = securityUtils.getUsuarioAutenticado();

				String mensaje = String.format("🔄 Actualización automática: %d lotes marcados como VENCIDOS",
						actualizados);

				com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest = com.example.proyecto.app.dto.request.BitacoraComunicacionRequest
						.builder().usuarioId(usuario.getId()).mensaje(mensaje)
						.tipo(BitacoraComunicacion.Tipo.incidencia).build();

				bitacoraService.crearMensaje(bitacoraRequest);
				log.info("Mensaje de bitácora creado para actualización de lotes vencidos: {} lotes", actualizados);
			} catch (Exception e) {
				log.error("Error al crear mensaje en bitácora para lotes vencidos: {}", e.getMessage());
			}
		}

		return actualizados;
	}

	// ==============================
	// AJUSTE MANUAL DE STOCK
	// ==============================

	@Override
	@Transactional
	public void ajustarStock(Integer loteId, Integer cantidad, Integer usuarioId, String tipoMovimiento,
			String observacion) {
		if (cantidad == 0) {
			throw new ParametroInvalidoException("La cantidad de ajuste no puede ser cero.");
		}

		if (observacion == null || observacion.trim().isEmpty()) {
			throw new ParametroInvalidoException("Debe proporcionar una observación para el ajuste de stock.");
		}

		Lote lote = loteRepository.findById(loteId)
				.orElseThrow(() -> new ResourceNotFoundException("Lote con ID " + loteId + " no encontrado."));

		int nuevaCantidad = lote.getCantidad() + cantidad;

		if (nuevaCantidad < 0) {
			throw new ParametroInvalidoException("El ajuste resultaría en stock negativo. Stock actual: "
					+ lote.getCantidad() + ", ajuste: " + cantidad);
		}

		int cantidadAnterior = lote.getCantidad();
		String nombreProducto = lote.getProducto().getNombre();
		String numeroLote = lote.getLote();

		lote.setCantidad(nuevaCantidad);
		loteRepository.save(lote);

		Usuario usuario = usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + usuarioId + " no encontrado."));

		// Crear movimiento de stock
		com.example.proyecto.app.entity.MovimientoStock.TipoMovimiento tipo;
		boolean esMerma = "merma".equalsIgnoreCase(tipoMovimiento);

		if (esMerma) {
			tipo = com.example.proyecto.app.entity.MovimientoStock.TipoMovimiento.merma;
			if (cantidad > 0) {
				throw new ParametroInvalidoException("Las mermas solo pueden reducir el stock (cantidad negativa).");
			}
		} else {
			tipo = com.example.proyecto.app.entity.MovimientoStock.TipoMovimiento.ajuste;
		}

		com.example.proyecto.app.entity.MovimientoStock movimiento = com.example.proyecto.app.entity.MovimientoStock
				.builder().lote(lote).usuario(usuario).tipoMovimiento(tipo).cantidad(Math.abs(cantidad))
				.costoUnitario(lote.getPrecioCompra())
				.observacion(
						observacion + " (Stock anterior: " + cantidadAnterior + ", Stock nuevo: " + nuevaCantidad + ")")
				.build();

		movimientoStockRepository.save(movimiento);

		log.info("Ajuste de stock realizado en lote ID: {}. Tipo: {}, Cantidad ajustada: {}, Nueva cantidad: {}",
				loteId, tipoMovimiento, cantidad, nuevaCantidad);

		if (nuevaCantidad == 0 && lote.getEstado() == Lote.EstadoLote.activo) {
			lote.setEstado(Lote.EstadoLote.agotado);
			loteRepository.save(lote);
			log.info("Lote ID: {} actualizado a estado 'agotado' tras ajuste", loteId);
		}

		// ==============================================================
		// CREAR MENSAJE EN BITÁCORA
		// ==============================================================
		try {
			String tipoTexto = esMerma ? "MERMA" : "AJUSTE";
			String mensaje = String.format("%s de stock: %s (Lote %s) - %s %d unidades - Nuevo stock: %d - %s",
					tipoTexto, nombreProducto, numeroLote, esMerma ? "🔻 Reducción de" : "📊 Ajuste de",
					Math.abs(cantidad), nuevaCantidad, observacion);

			BitacoraComunicacion.Tipo tipoBitacora = esMerma ? BitacoraComunicacion.Tipo.incidencia
					: BitacoraComunicacion.Tipo.novedad;

			com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest = com.example.proyecto.app.dto.request.BitacoraComunicacionRequest
					.builder().usuarioId(usuario.getId()).mensaje(mensaje).tipo(tipoBitacora).build();

			bitacoraService.crearMensaje(bitacoraRequest);
			log.info("Mensaje de bitácora creado para ajuste de stock en lote ID: {}", loteId);
		} catch (Exception e) {
			log.error("Error al crear mensaje en bitácora para ajuste de stock: {}", e.getMessage());
		}
	}
}