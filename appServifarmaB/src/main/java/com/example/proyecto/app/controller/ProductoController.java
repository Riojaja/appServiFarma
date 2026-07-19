package com.example.proyecto.app.controller;

import com.example.proyecto.app.dto.request.ProductoRequest;
import com.example.proyecto.app.dto.response.MensajeResponse;
import com.example.proyecto.app.dto.response.ProductoResponse;
import com.example.proyecto.app.service.ProductoService;
import com.example.proyecto.app.service.impl.ProductoImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

	private final ProductoService productoService;
	private final ProductoImportService productoImportService;

	// ============================================================
	// 1. OPERACIONES CRUD BÁSICAS
	// ============================================================

	@PostMapping
	public ResponseEntity<ProductoResponse> crearProducto(@Valid @RequestBody ProductoRequest request) {
		log.debug("Solicitud de creación de producto: {}", request.getNombre());
		ProductoResponse response = productoService.crearProducto(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ProductoResponse> actualizarProducto(@PathVariable Integer id,
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

	// ============================================================
	// 2. BÚSQUEDAS Y FILTROS
	// ============================================================

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
		log.debug("Solicitud de búsqueda de productos por texto: {}", texto);
		List<ProductoResponse> responses = productoService.buscarPorNombreOCodigo(texto);
		return ResponseEntity.ok(responses);
	}

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

	// ============================================================
	// 3. CONSULTAS DE STOCK Y ALERTAS
	// ============================================================

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

	@GetMapping("/existe")
	public ResponseEntity<Boolean> existePorCodigoBarras(@RequestParam String codigo) {
		log.debug("Solicitud de verificación de existencia de producto con código de barras: {}", codigo);
		boolean existe = productoService.existePorCodigoBarras(codigo);
		return ResponseEntity.ok(existe);
	}

	// ============================================================
	// 4. GESTIÓN DE IMÁGENES (NUEVO)
	// ============================================================

	/**
	 * Sube una imagen desde el archivo local del usuario. La imagen se guarda en el
	 * servidor y se asigna al producto.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{id}/imagen")
	public ResponseEntity<MensajeResponse> subirImagenProducto(@PathVariable Integer id,
			@RequestParam("imagen") MultipartFile imagen) {
		try {
			// Verificar que el producto existe
			productoService.obtenerProductoPorId(id);

			// Guardar la imagen localmente
			String rutaRelativa = productoImportService.guardarImagenLocal(imagen, id);

			// Actualizar el producto con la nueva ruta
			productoService.actualizarImagenProducto(id, rutaRelativa);

			return ResponseEntity.ok(new MensajeResponse("Imagen subida correctamente: " + rutaRelativa));
		} catch (Exception e) {
			log.error("Error al subir imagen para producto ID {}: {}", id, e.getMessage());
			return ResponseEntity.badRequest().body(new MensajeResponse("Error al subir imagen: " + e.getMessage()));
		}
	}

	/**
	 * Actualiza la imagen del producto desde una URL externa. El sistema descarga
	 * la imagen y la guarda localmente.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{id}/imagen-url")
	public ResponseEntity<MensajeResponse> actualizarImagenDesdeUrl(@PathVariable Integer id,
			@RequestParam String url) {
		log.debug("Actualizando imagen desde URL para producto ID: {}", id);
		try {
			// 1. Verificar que el producto existe
			productoService.obtenerProductoPorId(id);

			// 2. Descargar y guardar la imagen desde la URL
			String rutaRelativa = productoImportService.guardarImagenDesdeUrl(url, id);

			// 3. Actualizar el producto con la nueva ruta de imagen
			productoService.actualizarImagenProducto(id, rutaRelativa);

			log.info("Imagen actualizada desde URL para producto ID: {} -> {}", id, rutaRelativa);
			return ResponseEntity.ok(new MensajeResponse("Imagen actualizada desde URL correctamente"));

		} catch (Exception e) {
			log.error("Error al actualizar imagen desde URL para producto ID {}: {}", id, e.getMessage());
			return ResponseEntity.badRequest()
					.body(new MensajeResponse("Error al actualizar imagen desde URL: " + e.getMessage()));
		}
	}

	// ============================================================
	// 5. IMPORTACIÓN MASIVA DE PRODUCTOS (NUEVO)
	// ============================================================

	/**
	 * Descarga la plantilla Excel para importar productos.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/importacion/plantilla")
	public ResponseEntity<InputStreamResource> descargarPlantilla() {
		try {
			ByteArrayInputStream in = productoImportService.generarPlantilla();
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Disposition", "attachment; filename=plantilla_productos.xlsx");
			return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(new InputStreamResource(in));
		} catch (Exception e) {
			log.error("Error al generar plantilla", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	/**
	 * Importa productos desde un archivo Excel/CSV.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/importacion/subir")
	public ResponseEntity<?> importarProductos(@RequestParam("archivo") MultipartFile archivo) {
		try {
			var resultado = productoImportService.importarProductos(archivo);
			return ResponseEntity.ok(resultado);
		} catch (Exception e) {
			log.error("Error al importar productos", e);
			return ResponseEntity.badRequest().body(new MensajeResponse("Error al importar: " + e.getMessage()));
		}
	}
}