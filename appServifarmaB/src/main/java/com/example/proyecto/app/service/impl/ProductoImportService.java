package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.dto.importacion.ErrorImportacion;
import com.example.proyecto.app.dto.importacion.ProductoImportDTO;
import com.example.proyecto.app.dto.importacion.ResultadoImportacion;
import com.example.proyecto.app.dto.request.ProductoRequest;
import com.example.proyecto.app.dto.response.ProductoResponse;
import com.example.proyecto.app.entity.Categoria;
import com.example.proyecto.app.entity.Fabricante;
import com.example.proyecto.app.entity.Producto;
import com.example.proyecto.app.exception.DuplicadoException;
import com.example.proyecto.app.mapper.ProductoMapper;
import com.example.proyecto.app.repository.CategoriaRepository;
import com.example.proyecto.app.repository.FabricanteRepository;
import com.example.proyecto.app.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoImportService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final FabricanteRepository fabricanteRepository;
    private final ProductoMapper productoMapper;

    // ============================================================
    // GENERACIÓN DE PLANTILLA
    // ============================================================

    public ByteArrayInputStream generarPlantilla() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Productos");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.TEAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            String[] columnas = {
                "codigo_barras", "nombre", "principio_activo", "categoria", "fabricante",
                "precio_compra", "precio_venta", "stock_minimo", "stock_maximo",
                "unidad_medida", "concentracion", "presentacion", "es_generico",
                "requiere_receta", "imagen_url"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i), 4000));
            }

            Row ejemplo = sheet.createRow(1);
            ejemplo.createCell(0).setCellValue("7891234567890");
            ejemplo.createCell(1).setCellValue("Paracetamol 500mg x10");
            ejemplo.createCell(2).setCellValue("Paracetamol");
            ejemplo.createCell(3).setCellValue("Analgésicos");
            ejemplo.createCell(4).setCellValue("Pfizer");
            ejemplo.createCell(5).setCellValue(3.50);
            ejemplo.createCell(6).setCellValue(5.00);
            ejemplo.createCell(7).setCellValue(10);
            ejemplo.createCell(8).setCellValue(100);
            ejemplo.createCell(9).setCellValue("Tabletas");
            ejemplo.createCell(10).setCellValue("500mg");
            ejemplo.createCell(11).setCellValue("x10");
            ejemplo.createCell(12).setCellValue("No");
            ejemplo.createCell(13).setCellValue("No");
            ejemplo.createCell(14).setCellValue("https://ejemplo.com/imagen.jpg");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // ============================================================
    // LECTURA DE ARCHIVO
    // ============================================================

    private List<ProductoImportDTO> leerArchivo(MultipartFile archivo) throws IOException {
        List<ProductoImportDTO> lista = new ArrayList<>();
        try (InputStream is = archivo.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            int filaNum = 0;
            for (Row row : sheet) {
                if (filaNum == 0) { filaNum++; continue; }
                ProductoImportDTO dto = new ProductoImportDTO();
                dto.setFila(filaNum);
                dto.setCodigoBarras(getStringCell(row, 0));
                dto.setNombre(getStringCell(row, 1));
                dto.setPrincipioActivo(getStringCell(row, 2));
                dto.setCategoriaNombre(getStringCell(row, 3));
                dto.setFabricanteNombre(getStringCell(row, 4));
                dto.setPrecioCompra(getDoubleCell(row, 5));
                dto.setPrecioVenta(getDoubleCell(row, 6));
                dto.setStockMinimo(getIntegerCell(row, 7));
                dto.setStockMaximo(getIntegerCell(row, 8));
                dto.setUnidadMedida(getStringCell(row, 9));
                dto.setConcentracion(getStringCell(row, 10));
                dto.setPresentacion(getStringCell(row, 11));
                dto.setEsGenerico(getBooleanCell(row, 12));
                dto.setRequiereReceta(getBooleanCell(row, 13));
                dto.setImagenUrl(getStringCell(row, 14));
                if ((dto.getCodigoBarras() == null || dto.getCodigoBarras().isEmpty()) &&
                    (dto.getNombre() == null || dto.getNombre().isEmpty())) {
                    filaNum++;
                    continue;
                }
                lista.add(dto);
                filaNum++;
            }
        }
        return lista;
    }

    // ============================================================
    // VALIDACIONES
    // ============================================================

    private void validarFila(ProductoImportDTO dto) {
        if (isEmpty(dto.getCodigoBarras())) {
            throw new IllegalArgumentException("Código de barras es obligatorio (fila " + dto.getFila() + ")");
        }
        if (isEmpty(dto.getNombre())) {
            throw new IllegalArgumentException("Nombre es obligatorio (fila " + dto.getFila() + ")");
        }
        if (dto.getPrecioCompra() == null) {
            throw new IllegalArgumentException("Precio de compra es obligatorio (fila " + dto.getFila() + ")");
        }
        if (dto.getPrecioVenta() == null) {
            throw new IllegalArgumentException("Precio de venta es obligatorio (fila " + dto.getFila() + ")");
        }
        if (isEmpty(dto.getCategoriaNombre())) {
            throw new IllegalArgumentException("Categoría es obligatoria (fila " + dto.getFila() + ")");
        }
        if (productoRepository.existsByCodigoBarras(dto.getCodigoBarras())) {
            throw new DuplicadoException("Código de barras '" + dto.getCodigoBarras() +
                    "' ya existe en el sistema (fila " + dto.getFila() + ")");
        }
        if (dto.getPrecioCompra() <= 0) {
            throw new IllegalArgumentException("Precio de compra debe ser mayor que 0 (fila " + dto.getFila() + ")");
        }
        if (dto.getPrecioVenta() <= 0) {
            throw new IllegalArgumentException("Precio de venta debe ser mayor que 0 (fila " + dto.getFila() + ")");
        }
        if (dto.getPrecioVenta() <= dto.getPrecioCompra()) {
            throw new IllegalArgumentException("Precio de venta debe ser mayor que precio de compra (fila " + dto.getFila() + ")");
        }
        if (dto.getStockMinimo() != null && dto.getStockMaximo() != null &&
            dto.getStockMinimo() > dto.getStockMaximo()) {
            throw new IllegalArgumentException("Stock mínimo no puede ser mayor que stock máximo (fila " + dto.getFila() + ")");
        }
        if (!isEmpty(dto.getImagenUrl()) && !dto.getImagenUrl().startsWith("http") &&
            !dto.getImagenUrl().startsWith("/")) {
            throw new IllegalArgumentException("URL de imagen inválida (fila " + dto.getFila() + ")");
        }
    }

    private boolean isEmpty(String str) { return str == null || str.trim().isEmpty(); }

    // ============================================================
    // MAPEO A PRODUCTOREQUEST
    // ============================================================

    private ProductoRequest mapearARequest(ProductoImportDTO dto) {
        Integer categoriaId = obtenerOCrearCategoria(dto.getCategoriaNombre());
        Integer fabricanteId = null;
        if (!isEmpty(dto.getFabricanteNombre())) {
            fabricanteId = obtenerOCrearFabricante(dto.getFabricanteNombre());
        }
        boolean esGenerico = dto.getEsGenerico() != null ? dto.getEsGenerico() : false;
        BigDecimal precioCompra = dto.getPrecioCompra() != null ? BigDecimal.valueOf(dto.getPrecioCompra()) : BigDecimal.ZERO;
        BigDecimal precioVenta = dto.getPrecioVenta() != null ? BigDecimal.valueOf(dto.getPrecioVenta()) : BigDecimal.ZERO;

        return ProductoRequest.builder()
                .codigoBarras(dto.getCodigoBarras())
                .nombre(dto.getNombre())
                .principioActivo(dto.getPrincipioActivo())
                .categoriaId(categoriaId)
                .fabricanteId(fabricanteId)
                .precioCompraActual(precioCompra)
                .precioVentaActual(precioVenta)
                .stockMinimo(dto.getStockMinimo() != null ? dto.getStockMinimo() : 0)
                .imagen(dto.getImagenUrl())
                .esGenerico(esGenerico)
                .build();
    }

    // ============================================================
    // CREACIÓN AUTOMÁTICA DE CATEGORÍAS Y FABRICANTES
    // ============================================================

    private Integer obtenerOCrearCategoria(String nombre) {
        String nombreNormalizado = nombre.trim();
        return categoriaRepository.findByNombreIgnoreCase(nombreNormalizado)
                .map(Categoria::getId)
                .orElseGet(() -> {
                    Categoria nueva = Categoria.builder()
                            .nombre(nombreNormalizado)
                            .descripcion("Creada automáticamente por importación")
                            .build();
                    return categoriaRepository.save(nueva).getId();
                });
    }

    private Integer obtenerOCrearFabricante(String nombre) {
        String nombreNormalizado = nombre.trim();
        return fabricanteRepository.findByNombreIgnoreCase(nombreNormalizado)
                .map(Fabricante::getId)
                .orElseGet(() -> {
                    Fabricante nuevo = Fabricante.builder()
                            .nombre(nombreNormalizado)
                            .contacto("Importado automáticamente")
                            .build();
                    return fabricanteRepository.save(nuevo).getId();
                });
    }

    // ============================================================
    // IMPORTACIÓN (CORREGIDA - SIN DEPENDENCIA CÍCLICA)
    // ============================================================

    @Transactional
    public ResultadoImportacion importarProductos(MultipartFile archivo) throws IOException {
        List<ProductoImportDTO> filas = leerArchivo(archivo);
        ResultadoImportacion resultado = ResultadoImportacion.builder()
                .totalFilas(filas.size())
                .importados(0)
                .errores(0)
                .erroresDetalle(new ArrayList<>())
                .productosImportados(new ArrayList<>())
                .build();

        for (ProductoImportDTO dto : filas) {
            try {
                validarFila(dto);
                ProductoRequest request = mapearARequest(dto);
                // Mapear a entidad
                Producto producto = productoMapper.toEntity(request);
                // Setear relaciones (categoría y fabricante)
                if (request.getCategoriaId() != null) {
                    Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                            .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
                    producto.setCategoria(categoria);
                }
                if (request.getFabricanteId() != null) {
                    Fabricante fabricante = fabricanteRepository.findById(request.getFabricanteId())
                            .orElseThrow(() -> new RuntimeException("Fabricante no encontrado"));
                    producto.setFabricante(fabricante);
                }
                // Guardar producto
                Producto saved = productoRepository.save(producto);
                resultado.getProductosImportados().add(productoMapper.toResponse(saved));
                resultado.setImportados(resultado.getImportados() + 1);
                log.debug("Producto importado: {} (ID: {})", dto.getNombre(), saved.getId());
            } catch (Exception e) {
                resultado.setErrores(resultado.getErrores() + 1);
                resultado.getErroresDetalle().add(ErrorImportacion.builder()
                        .fila(dto.getFila())
                        .mensaje(e.getMessage())
                        .datos(obtenerDatosResumidos(dto))
                        .build());
                log.warn("Error en fila {}: {}", dto.getFila(), e.getMessage());
            }
        }
        log.info("Importación finalizada: {} importados, {} errores de {} filas",
                resultado.getImportados(), resultado.getErrores(), resultado.getTotalFilas());
        return resultado;
    }

    private String obtenerDatosResumidos(ProductoImportDTO dto) {
        return "Código: " + dto.getCodigoBarras() + " | Nombre: " + dto.getNombre() + " | Categoría: " + dto.getCategoriaNombre();
    }

    // ============================================================
    // MÉTODOS AUXILIARES PARA LEER CELDAS
    // ============================================================

    private String getStringCell(Row row, int idx) {
        Cell cell = row.getCell(idx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((long) cell.getNumericCellValue());
        return null;
    }

    private Double getDoubleCell(Row row, int idx) {
        Cell cell = row.getCell(idx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            try { return Double.parseDouble(cell.getStringCellValue().trim().replace(",", ".")); }
            catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    private Integer getIntegerCell(Row row, int idx) {
        Double d = getDoubleCell(row, idx);
        return d != null ? d.intValue() : null;
    }

    private Boolean getBooleanCell(Row row, int idx) {
        String val = getStringCell(row, idx);
        if (val == null) return null;
        val = val.toLowerCase().trim();
        return "sí".equals(val) || "si".equals(val) || "true".equals(val) || "1".equals(val) || "yes".equals(val);
    }

    // ============================================================
    // MANEJO DE IMÁGENES (LOCAL Y URL)
    // ============================================================

    public String guardarImagenLocal(MultipartFile imagen, Integer productoId) throws IOException {
        if (imagen.getContentType() == null || !imagen.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen válida");
        }
        if (imagen.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("La imagen no puede superar los 5 MB");
        }
        String extension = obtenerExtension(imagen.getOriginalFilename());
        String nombreArchivo = "producto_" + productoId + "_" + System.currentTimeMillis() + "." + extension;
        Path uploadDir = Paths.get("uploads/productos/");
        if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
        Path destino = uploadDir.resolve(nombreArchivo);
        Files.copy(imagen.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        log.info("Imagen guardada: {}", destino.toString());
        return "/uploads/productos/" + nombreArchivo;
    }

    public String guardarImagenDesdeUrl(String url, Integer productoId) throws IOException {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("La URL no puede estar vacía");
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException("La URL debe ser válida (http o https)");
        }
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("No se pudo descargar la imagen. Código: " + connection.getResponseCode());
        }
        byte[] imageBytes = connection.getInputStream().readAllBytes();
        connection.disconnect();
        String extension = obtenerExtensionDesdeUrl(url);
        if (extension == null) {
            String contentType = connection.getContentType();
            if (contentType != null) {
                if (contentType.contains("jpeg") || contentType.contains("jpg")) extension = "jpg";
                else if (contentType.contains("png")) extension = "png";
                else if (contentType.contains("gif")) extension = "gif";
                else if (contentType.contains("webp")) extension = "webp";
                else extension = "jpg";
            } else {
                extension = "jpg";
            }
        }
        String nombreArchivo = "producto_" + productoId + "_" + System.currentTimeMillis() + "." + extension;
        Path uploadDir = Paths.get("uploads/productos/");
        if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
        Path destino = uploadDir.resolve(nombreArchivo);
        Files.write(destino, imageBytes);
        log.info("Imagen descargada desde URL y guardada: {}", destino.toString());
        return "/uploads/productos/" + nombreArchivo;
    }

    private String obtenerExtension(String filename) {
        if (filename == null) return "jpg";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "jpg";
    }

    private String obtenerExtensionDesdeUrl(String url) {
        if (url == null) return null;
        int queryIndex = url.indexOf('?');
        String path = queryIndex > 0 ? url.substring(0, queryIndex) : url;
        int lastDot = path.lastIndexOf('.');
        if (lastDot > 0) {
            String ext = path.substring(lastDot + 1).toLowerCase();
            if (ext.matches("jpg|jpeg|png|gif|webp|bmp|svg")) return ext;
        }
        return null;
    }
}