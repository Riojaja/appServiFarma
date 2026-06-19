package com.example.proyecto.app.auth;

/**
 * Servicio de autenticación y gestión de sesiones.
 * Proporciona métodos para login, refresco de token, cambio de contraseña y cierre de sesión.
 */
public interface AuthService {

    /**
     * Autentica a un usuario con sus credenciales.
     * 
     * @param request Objeto con el nombre de usuario y la contraseña.
     * @return AuthResponse con el token JWT, datos del usuario y rol.
     * @throws AuthException Si las credenciales son incorrectas o el usuario está inactivo.
     */
    AuthResponse autenticar(LoginRequest request);

    /**
     * Renueva un token JWT utilizando un refresh token (opcional).
     * 
     * @param refreshToken El refresh token válido.
     * @return AuthResponse con el nuevo token JWT.
     * @throws AuthException Si el refresh token es inválido o ha expirado.
     */
    AuthResponse refrescarToken(String refreshToken);

    /**
     * Cambia la contraseña de un usuario autenticado.
     * 
     * @param request Objeto con la contraseña actual, la nueva y su confirmación.
     * @param nombreUsuario El nombre del usuario que solicita el cambio.
     * @throws AuthException Si la contraseña actual no coincide o la nueva no cumple los requisitos.
     */
    void cambiarContrasena(ChangePasswordRequest request, String nombreUsuario);

    /**
     * Cierra la sesión del usuario (invalida el token en el lado del servidor, si se implementa).
     * 
     * @param token El token JWT a invalidar.
     */
    void cerrarSesion(String token);
}