package com.example.proyecto.app.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Utilidad para el manejo de archivos, directorios y operaciones de I/O.
 * Proporciona métodos para crear archivos temporales, leer/escribir bytes,
 * limpiar recursos y validar extensiones.
 */
public final class ArchivoUtils {

    private static final String TEMP_PREFIX = "servifarma_";
    private static final String TEMP_SUFFIX = ".tmp";

    private ArchivoUtils() {
        // Constructor privado para evitar instanciación
    }

    // ==============================
    // CREACIÓN Y MANEJO DE ARCHIVOS TEMPORALES
    // ==============================

    /**
     * Crea un archivo temporal con el prefijo y sufijo por defecto.
     *
     * @return Archivo temporal creado.
     * @throws IOException Si ocurre un error al crear el archivo.
     */
    public static File crearArchivoTemporal() throws IOException {
        return crearArchivoTemporal(TEMP_PREFIX, TEMP_SUFFIX);
    }

    /**
     * Crea un archivo temporal con un prefijo y sufijo personalizados.
     *
     * @param prefijo Prefijo del nombre del archivo (ej. "reporte_").
     * @param sufijo  Sufijo del nombre del archivo (ej. ".pdf").
     * @return Archivo temporal creado.
     * @throws IOException Si ocurre un error al crear el archivo.
     */
    public static File crearArchivoTemporal(String prefijo, String sufijo) throws IOException {
        if (prefijo == null) prefijo = TEMP_PREFIX;
        if (sufijo == null) sufijo = TEMP_SUFFIX;
        return File.createTempFile(prefijo, sufijo);
    }

    /**
     * Crea un archivo temporal en un directorio específico.
     *
     * @param directorio Directorio donde se creará el archivo.
     * @param prefijo    Prefijo del nombre del archivo.
     * @param sufijo     Sufijo del nombre del archivo.
     * @return Archivo temporal creado.
     * @throws IOException Si ocurre un error al crear el archivo.
     */
    public static File crearArchivoTemporal(File directorio, String prefijo, String sufijo) throws IOException {
        if (directorio == null) {
            return crearArchivoTemporal(prefijo, sufijo);
        }
        if (!directorio.exists() && !directorio.mkdirs()) {
            throw new IOException("No se pudo crear el directorio: " + directorio.getAbsolutePath());
        }
        return File.createTempFile(prefijo, sufijo, directorio);
    }

    // ==============================
    // LECTURA Y ESCRITURA DE BYTES
    // ==============================

    /**
     * Lee el contenido de un archivo y lo retorna como un arreglo de bytes.
     *
     * @param file Archivo a leer.
     * @return Arreglo de bytes con el contenido del archivo.
     * @throws IOException Si ocurre un error al leer el archivo.
     */
    public static byte[] leerBytes(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("El archivo no puede ser nulo.");
        }
        if (!file.exists()) {
            throw new FileNotFoundException("El archivo no existe: " + file.getAbsolutePath());
        }
        return Files.readAllBytes(file.toPath());
    }

    /**
     * Escribe un arreglo de bytes en un archivo.
     *
     * @param file Archivo donde se escribirá el contenido.
     * @param data Datos a escribir.
     * @throws IOException Si ocurre un error al escribir el archivo.
     */
    public static void escribirBytes(File file, byte[] data) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("El archivo no puede ser nulo.");
        }
        if (data == null) {
            throw new IllegalArgumentException("Los datos no pueden ser nulos.");
        }
        Files.write(file.toPath(), data);
    }

    /**
     * Escribe un arreglo de bytes en un archivo, creando los directorios padre si no existen.
     *
     * @param file Archivo donde se escribirá el contenido.
     * @param data Datos a escribir.
     * @throws IOException Si ocurre un error al escribir el archivo.
     */
    public static void escribirBytesConDirectorios(File file, byte[] data) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("El archivo no puede ser nulo.");
        }
        if (data == null) {
            throw new IllegalArgumentException("Los datos no pueden ser nulos.");
        }
        Path parent = file.toPath().getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        Files.write(file.toPath(), data);
    }

    // ==============================
    // ELIMINACIÓN DE ARCHIVOS
    // ==============================

    /**
     * Elimina un archivo de forma segura.
     *
     * @param file Archivo a eliminar.
     * @return true si se eliminó correctamente, false en caso contrario.
     */
    public static boolean eliminarArchivo(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        return file.delete();
    }

    /**
     * Elimina una colección de archivos de forma segura.
     *
     * @param files Colección de archivos a eliminar.
     */
    public static void eliminarArchivos(Collection<File> files) {
        if (files == null) {
            return;
        }
        for (File file : files) {
            eliminarArchivo(file);
        }
    }

    /**
     * Elimina un archivo cuando la JVM termina (registro de shutdown hook).
     *
     * @param file Archivo a eliminar al finalizar.
     */
    public static void eliminarAlFinalizar(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        file.deleteOnExit();
    }

    /**
     * Vacía el contenido de un directorio (elimina todos los archivos y subdirectorios dentro de él).
     *
     * @param directorio Directorio a vaciar.
     * @throws IOException Si ocurre un error al acceder al directorio.
     */
    public static void vaciarDirectorio(File directorio) throws IOException {
        if (directorio == null || !directorio.exists()) {
            return;
        }
        if (!directorio.isDirectory()) {
            throw new IllegalArgumentException("El archivo no es un directorio: " + directorio.getAbsolutePath());
        }
        File[] files = directorio.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    vaciarDirectorio(file);
                }
                eliminarArchivo(file);
            }
        }
    }

    // ==============================
    // VALIDACIONES Y EXTENSIONES
    // ==============================

    /**
     * Valida que los datos no sean nulos ni estén vacíos.
     *
     * @param data Arreglo de bytes a validar.
     * @return true si es válido, false en caso contrario.
     */
    public static boolean validarContenido(byte[] data) {
        return data != null && data.length > 0;
    }

    /**
     * Valida que los datos no sean nulos ni estén vacíos, lanzando una excepción si falla.
     *
     * @param data Arreglo de bytes a validar.
     * @throws IllegalArgumentException Si los datos son nulos o vacíos.
     */
    public static void validarContenidoOExcepcion(byte[] data) {
        if (!validarContenido(data)) {
            throw new IllegalArgumentException("Los datos no pueden ser nulos o vacíos.");
        }
    }

    /**
     * Obtiene la extensión de un nombre de archivo (sin el punto).
     *
     * @param nombreArchivo Nombre del archivo (ej. "reporte.pdf").
     * @return Extensión en minúsculas (ej. "pdf"), o cadena vacía si no tiene.
     */
    public static String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isEmpty()) {
            return "";
        }
        int lastDot = nombreArchivo.lastIndexOf('.');
        if (lastDot == -1 || lastDot == nombreArchivo.length() - 1) {
            return "";
        }
        return nombreArchivo.substring(lastDot + 1).toLowerCase();
    }

    /**
     * Obtiene el nombre de un archivo sin su extensión.
     *
     * @param nombreArchivo Nombre del archivo (ej. "reporte.pdf").
     * @return Nombre sin extensión (ej. "reporte").
     */
    public static String obtenerNombreSinExtension(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isEmpty()) {
            return nombreArchivo;
        }
        int lastDot = nombreArchivo.lastIndexOf('.');
        if (lastDot == -1) {
            return nombreArchivo;
        }
        return nombreArchivo.substring(0, lastDot);
    }

    /**
     * Verifica si un archivo tiene una extensión específica (ignorando mayúsculas).
     *
     * @param nombreArchivo  Nombre del archivo.
     * @param extensionBuscada Extensión a verificar (ej. "pdf", "xlsx").
     * @return true si la extensión coincide, false en caso contrario.
     */
    public static boolean tieneExtension(String nombreArchivo, String extensionBuscada) {
        if (nombreArchivo == null || extensionBuscada == null) {
            return false;
        }
        return obtenerExtension(nombreArchivo).equals(extensionBuscada.toLowerCase());
    }

    /**
     * Verifica si el nombre de archivo corresponde a un archivo de imagen (jpg, png, jpeg, gif, bmp, svg).
     *
     * @param nombreArchivo Nombre del archivo.
     * @return true si es una imagen, false en caso contrario.
     */
    public static boolean esImagen(String nombreArchivo) {
        String ext = obtenerExtension(nombreArchivo);
        return ext.matches("jpg|jpeg|png|gif|bmp|svg|webp");
    }

    /**
     * Verifica si el nombre de archivo corresponde a un documento de oficina (pdf, docx, xlsx, pptx, odt, ods).
     *
     * @param nombreArchivo Nombre del archivo.
     * @return true si es un documento de oficina, false en caso contrario.
     */
    public static boolean esDocumento(String nombreArchivo) {
        String ext = obtenerExtension(nombreArchivo);
        return ext.matches("pdf|docx|xlsx|pptx|odt|ods|rtf|txt|csv");
    }
}