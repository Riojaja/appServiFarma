package com.example.proyecto.app.controller;

import com.example.proyecto.app.dto.request.AjusteStockRequest;
import com.example.proyecto.app.dto.request.LoteRequest;
import com.example.proyecto.app.dto.response.LoteResponse;
import com.example.proyecto.app.dto.response.MensajeResponse;
import com.example.proyecto.app.entity.Lote;
import com.example.proyecto.app.service.LoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/lotes")
@RequiredArgsConstructor
public class LoteController {

	private final LoteService loteService;

	// ==============================
	// OPERACIONES CRUD BÁSICAS
	// ==============================

	@PostMapping
	public ResponseEntity<LoteResponse> crearLote(@Valid @RequestBody LoteRequest request) {
		log.debug("Solicitud de creación de lote: {}", request.getLote());
		LoteResponse response = loteService.crearLote(request);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public ResponseEntity<LoteResponse> actualizarLote(@PathVariable Integer id,
			@Valid @RequestBody LoteRequest request) {
		log.debug("Solicitud de actualización de lote con ID: {}", id);
		LoteResponse response = loteService.actualizarLote(id, request);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<LoteResponse> obtenerLotePorId(@PathVariable Integer id) {
		log.debug("Solicitud de lote con ID: {}", id);
		LoteResponse response = loteService.obtenerLotePorId(id);
		return ResponseEntity.ok(response);
	}

	@GetMapping
	public ResponseEntity<List<LoteResponse>> listarTodos() {
		log.debug("Solicitud de listado de todos los lotes");
		List<LoteResponse> responses = loteService.listarTodos();
		return ResponseEntity.ok(responses);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<MensajeResponse> eliminarLote(@PathVariable Integer id) {
		log.debug("Solicitud de eliminación de lote con ID: {}", id);
		loteService.eliminarLote(id);
		return ResponseEntity.ok(new MensajeResponse("Lote eliminado correctamente"));
	}

	// ==============================
	// CONSULTAS ESPECÍFICAS
	// ==============================

	@GetMapping("/producto/{productoId}")
	public ResponseEntity<List<LoteResponse>> listarPorProducto(@PathVariable Integer productoId) {
		log.debug("Solicitud de lotes por producto ID: {}", productoId);
		List<LoteResponse> responses = loteService.listarPorProducto(productoId);
		return ResponseEntity.ok(responses);
	}

	@GetMapping("/estado/{estado}")
	public ResponseEntity<List<LoteResponse>> listarPorEstado(@PathVariable Lote.EstadoLote estado) {
		log.debug("Solicitud de lotes por estado: {}", estado);
		List<LoteResponse> responses = loteService.listarPorEstado(estado);
		return ResponseEntity.ok(responses);
	}

	@GetMapping("/numero/{numeroLote}")
	public ResponseEntity<LoteResponse> buscarPorLote(@PathVariable String numeroLote) {
		log.debug("Solicitud de lote por número: {}", numeroLote);
		LoteResponse response = loteService.buscarPorLote(numeroLote);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/numero/contiene/{numeroLote}")
	public ResponseEntity<List<LoteResponse>> buscarPorLoteContaining(@PathVariable String numeroLote) {
		log.debug("Solicitud de búsqueda de lotes por número que contenga: {}", numeroLote);
		List<LoteResponse> responses = loteService.buscarPorLoteContaining(numeroLote);
		return ResponseEntity.ok(responses);
	}

	@GetMapping("/proximos-a-vencer")
	public ResponseEntity<List<LoteResponse>> obtenerLotesProximosAVencer(
			@RequestParam(defaultValue = "30") int diasAnticipacion) {
		log.debug("Solicitud de lotes próximos a vencer con {} días de anticipación", diasAnticipacion);
		List<LoteResponse> responses = loteService.obtenerLotesProximosAVencer(diasAnticipacion);
		return ResponseEntity.ok(responses);
	}

	@GetMapping("/vencidos")
	public ResponseEntity<List<LoteResponse>> obtenerLotesVencidos() {
		log.debug("Solicitud de lotes vencidos");
		List<LoteResponse> responses = loteService.obtenerLotesVencidos();
		return ResponseEntity.ok(responses);
	}

	// ==============================
	// OPERACIONES DE ACTUALIZACIÓN DE ESTADO
	// ==============================

	@PatchMapping("/{id}/deteriorado")
	public ResponseEntity<MensajeResponse> marcarComoDeteriorado(@PathVariable Integer id) {
		log.debug("Solicitud de marcar lote ID: {} como deteriorado", id);
		loteService.marcarComoDeteriorado(id);
		return ResponseEntity.ok(new MensajeResponse("Lote marcado como deteriorado correctamente"));
	}

	@PostMapping("/actualizar-vencidos")
	public ResponseEntity<MensajeResponse> actualizarLotesVencidos() {
		log.debug("Solicitud de actualización de lotes vencidos (tarea programada)");
		int actualizados = loteService.actualizarLotesVencidos();
		return ResponseEntity.ok(new MensajeResponse("Se actualizaron " + actualizados + " lotes a estado 'vencido'"));
	}

	// ==============================
	// AJUSTE DE STOCK (Manual)
	// ==============================

	@PatchMapping("/{id}/stock")
	public ResponseEntity<MensajeResponse> ajustarStock(@PathVariable Integer id,
			@Valid @RequestBody AjusteStockRequest request) {
		log.debug("Solicitud de ajuste de stock para lote ID: {}, tipo: {}, cantidad: {}", id,
				request.getTipoMovimiento(), request.getCantidad());

		// El ajuste manual de stock no está implementado en esta versión.
		// Para descontar stock usa el endpoint de ventas.
		// Para aumentar stock registra un nuevo lote.
		throw new UnsupportedOperationException("Ajuste de stock manual aún no implementado. "
				+ "Usa el endpoint de ventas para descontar o registra un nuevo lote para aumentar.");
	}
}