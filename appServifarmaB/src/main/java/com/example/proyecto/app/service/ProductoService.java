package com.example.proyecto.app.service;

import com.example.proyecto.app.dto.request.ProductoRequest;
import com.example.proyecto.app.dto.response.ProductoResponse;

import java.util.List;

public interface ProductoService {

    // ==============================
    // OPERACIONES CRUD BÁSICAS
    // ==============================

    /**
     * Registra un nuevo producto en el catálogo.
     * @param request Datos del producto (nombre, código de barras, principio activo, etc.)
     * @return ProductoResponse con los datos guardados.
     * @throws DuplicadoException Si ya existe un producto con el mismo código de barras.
     * @throws ResourceNotFoundException Si la categoría o el fabricante especificados no existen.
     */
    ProductoResponse crearProducto(ProductoRequest request);

    /**
     * Actualiza los datos de un producto existente.
     * @param id ID del producto a actualizar.
     * @param request Nuevos datos del producto.
     * @return ProductoResponse con los datos actualizados.
     * @throws ResourceNotFoundException Si el producto no existe.
     * @throws DuplicadoException Si se intenta cambiar el código de barras y ya existe otro con ese código.
     */
    ProductoResponse actualizarProducto(Integer id, ProductoRequest request);

    /**
     * Obtiene un producto por su ID.
     * @param id ID del producto.
     * @return ProductoResponse con los datos.
     * @throws ResourceNotFoundException Si el producto no existe.
     */
    ProductoResponse obtenerProductoPorId(Integer id);

    /**
     * Lista todos los productos, ordenados alfabéticamente por nombre (A-Z).
     * @return Lista de ProductoResponse.
     */
    List<ProductoResponse> listarTodos();

    /**
     * Elimina un producto del sistema (borrado físico).
     * @param id ID del producto a eliminar.
     * @throws ResourceNotFoundException Si el producto no existe.
     * @throws BusinessException Si el producto tiene lotes asociados (para preservar integridad).
     */
    void eliminarProducto(Integer id);

    // ==============================
    // BÚSQUEDAS POR CAMPOS ESPECÍFICOS
    // ==============================

    /**
     * Busca un producto por su código de barras (exacto).
     * Esencial para el lector de códigos de barras en el módulo de ventas (RF20).
     * @param codigoBarras Código de barras del producto.
     * @return ProductoResponse con los datos.
     * @throws ResourceNotFoundException Si no se encuentra el producto.
     */
    ProductoResponse buscarPorCodigoBarras(String codigoBarras);

    /**
     * Busca productos por nombre (coincidencia parcial, ignorando mayúsculas/minúsculas).
     * Fundamental para el buscador visual (RF7 y RF28).
     * @param nombre Cadena a buscar.
     * @return Lista de ProductoResponse que coinciden.
     */
    List<ProductoResponse> buscarPorNombre(String nombre);

    /**
     * Busca productos por principio activo (coincidencia parcial).
     * Útil para la vinculación de genéricos y comerciales (RF10).
     * @param principioActivo Principio activo a buscar.
     * @return Lista de ProductoResponse que coinciden.
     */
    List<ProductoResponse> buscarPorPrincipioActivo(String principioActivo);

    /**
     * Busca productos por nombre o código de barras (búsqueda combinada).
     * Ideal para el buscador principal de la interfaz de ventas.
     * @param texto Cadena a buscar (nombre o código de barras).
     * @return Lista de ProductoResponse que coinciden.
     */
    List<ProductoResponse> buscarPorNombreOCodigo(String texto);

    /**
     * Lista productos filtrados por categoría.
     * @param categoriaId ID de la categoría.
     * @return Lista de ProductoResponse que pertenecen a esa categoría.
     */
    List<ProductoResponse> buscarPorCategoria(Integer categoriaId);

    /**
     * Lista productos filtrados por fabricante.
     * @param fabricanteId ID del fabricante.
     * @return Lista de ProductoResponse que pertenecen a ese fabricante.
     */
    List<ProductoResponse> buscarPorFabricante(Integer fabricanteId);

    /**
     * Lista solo los productos genéricos.
     * @return Lista de ProductoResponse que son genéricos (es_generico = true).
     */
    List<ProductoResponse> listarGenericos();

    /**
     * Busca alternativas comerciales de un producto genérico (mismo principio activo).
     * @param productoId ID del producto genérico.
     * @return Lista de ProductoResponse de la misma familia (excluyendo el propio producto).
     */
    List<ProductoResponse> buscarAlternativasGenericas(Integer productoId);

    // ==============================
    // CONSULTAS DE STOCK Y ALERTAS (RF6)
    // ==============================

    /**
     * Obtiene el stock total (suma de todos los lotes activos) de un producto.
     * @param productoId ID del producto.
     * @return Cantidad total disponible en stock.
     */
    Integer obtenerStockActual(Integer productoId);

    /**
     * Lista productos que tienen stock por debajo del mínimo configurado (stock_minimo).
     * Útil para generar alertas de reposición.
     * @return Lista de ProductoResponse con stock bajo.
     */
    List<ProductoResponse> obtenerProductosConStockBajo();

    /**
     * Lista productos que no tienen stock disponible (ningún lote activo con cantidad > 0).
     * @return Lista de ProductoResponse sin stock.
     */
    List<ProductoResponse> obtenerProductosSinStock();

    // ==============================
    // VALIDACIONES
    // ==============================

    /**
     * Verifica si existe un producto con el código de barras dado.
     * @param codigoBarras Código de barras a verificar.
     * @return true si existe, false en caso contrario.
     */
    boolean existePorCodigoBarras(String codigoBarras);
}
