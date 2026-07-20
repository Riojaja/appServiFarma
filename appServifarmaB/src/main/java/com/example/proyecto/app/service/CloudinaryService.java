package com.example.proyecto.app.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Sube una imagen desde un archivo MultipartFile (subido por el usuario).
     */
    @SuppressWarnings("unchecked")
    public String uploadImage(MultipartFile file) throws IOException {
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return uploadResult.get("secure_url").toString();
    }

    /**
     * Sube una imagen desde una URL externa (descarga y sube a Cloudinary).
     */
    @SuppressWarnings("unchecked")
    public String uploadImageFromUrl(String imageUrl) throws IOException {
        // 1. Descargar la imagen desde la URL
        HttpURLConnection connection = (HttpURLConnection) new URL(imageUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("No se pudo descargar la imagen. Código: " + connection.getResponseCode());
        }

        byte[] imageBytes = connection.getInputStream().readAllBytes();
        connection.disconnect();

        // 2. Subir a Cloudinary
        Map<String, Object> uploadResult = cloudinary.uploader().upload(imageBytes, ObjectUtils.emptyMap());
        return uploadResult.get("secure_url").toString();
    }

    /**
     * Elimina una imagen de Cloudinary por su publicId.
     */
    @SuppressWarnings("unchecked")
    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}