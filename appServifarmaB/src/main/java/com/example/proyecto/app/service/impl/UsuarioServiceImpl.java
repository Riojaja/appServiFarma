package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.dto.request.UsuarioRequest;
import com.example.proyecto.app.dto.response.UsuarioResponse;
import com.example.proyecto.app.entity.BitacoraComunicacion;
import com.example.proyecto.app.entity.Rol;
import com.example.proyecto.app.entity.Usuario;
import com.example.proyecto.app.exception.DuplicadoException;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.mapper.UsuarioMapper;
import com.example.proyecto.app.repository.RolRepository;
import com.example.proyecto.app.repository.SesionUsuarioRepository;
import com.example.proyecto.app.repository.UsuarioRepository;
import com.example.proyecto.app.service.BitacoraComunicacionService;
import com.example.proyecto.app.service.ConfiguracionService;
import com.example.proyecto.app.service.UsuarioService;
import com.example.proyecto.app.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final SesionUsuarioRepository sesionRepository;
    private final BitacoraComunicacionService bitacoraService;
    private final SecurityUtils securityUtils;
    private final ConfiguracionService configuracionService; // ✅ Inyectado

    // ========== CRUD ==========

    @Override
    @Transactional
    public UsuarioResponse crearUsuario(UsuarioRequest request) {
        if (usuarioRepository.existsByUsuario(request.getUsuario())) {
            throw new DuplicadoException("El nombre de usuario '" + request.getUsuario() + "' ya está registrado.");
        }

        Rol rol = rolRepository.findById(request.getRolId())
                .orElseThrow(() -> new ResourceNotFoundException("Rol con ID " + request.getRolId() + " no encontrado."));

        Usuario usuario = usuarioMapper.toEntity(request);
        usuario.setRol(rol);
        usuario.setContrasena(passwordEncoder.encode(request.getContrasena()));
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);

        Usuario saved = usuarioRepository.save(usuario);
        log.info("Usuario creado: {} (ID: {})", saved.getUsuario(), saved.getId());
        registrarBitacora("📝 Usuario creado: " + saved.getUsuario() + " (ID: " + saved.getId() + ")",
                BitacoraComunicacion.Tipo.novedad);
        return usuarioMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UsuarioResponse actualizarUsuario(Integer id, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado."));

        if (!usuario.getUsuario().equals(request.getUsuario()) &&
                usuarioRepository.existsByUsuario(request.getUsuario())) {
            throw new DuplicadoException("El nombre de usuario '" + request.getUsuario() + "' ya está registrado.");
        }

        String nombreAnterior = usuario.getUsuario();
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setUsuario(request.getUsuario());

        if (!usuario.getRol().getId().equals(request.getRolId())) {
            Rol nuevoRol = rolRepository.findById(request.getRolId())
                    .orElseThrow(() -> new ResourceNotFoundException("Rol con ID " + request.getRolId() + " no encontrado."));
            usuario.setRol(nuevoRol);
        }

        // Si se desactiva, cerrar sesiones
        if (!request.getActivo() && usuario.isActivo()) {
            sesionRepository.invalidarSesionesPorUsuario(id);
            log.info("Sesiones del usuario {} invalidadas por desactivación", usuario.getUsuario());
        }
        usuario.setActivo(request.getActivo());

        Usuario updated = usuarioRepository.save(usuario);
        log.info("Usuario actualizado: {} (ID: {})", updated.getUsuario(), updated.getId());
        registrarBitacora("✏️ Usuario actualizado: " + nombreAnterior + " → " + updated.getUsuario() + " (ID: " + id + ")",
                BitacoraComunicacion.Tipo.novedad);
        return usuarioMapper.toResponse(updated);
    }

    @Override
    public UsuarioResponse obtenerUsuarioPorId(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado."));
        return usuarioMapper.toResponse(usuario);
    }

    @Override
    public UsuarioResponse obtenerUsuarioPorUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsuario(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario '" + username + "' no encontrado."));
        return usuarioMapper.toResponse(usuario);
    }

    @Override
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioResponse> listarPorEstado(boolean activo) {
        return (activo ? usuarioRepository.findByActivoTrue() : usuarioRepository.findByActivoFalse())
                .stream()
                .map(usuarioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioResponse> listarPorRol(Integer rolId) {
        if (!rolRepository.existsById(rolId)) {
            throw new ResourceNotFoundException("Rol con ID " + rolId + " no encontrado.");
        }
        return usuarioRepository.findByRolId(rolId).stream()
                .map(usuarioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioResponse> buscarPorNombre(String nombre) {
        return usuarioRepository.findByNombreCompletoContainingIgnoreCase(nombre).stream()
                .map(usuarioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cambiarEstadoUsuario(Integer id, boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado."));
        if (!activo && usuario.isActivo()) {
            sesionRepository.invalidarSesionesPorUsuario(id);
        }
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
        String mensaje = activo ? "Usuario activado" : "Usuario desactivado";
        log.info("{}: {} (ID: {})", mensaje, usuario.getUsuario(), id);
        registrarBitacora("🔒 " + mensaje + ": " + usuario.getUsuario() + " (ID: " + id + ")",
                BitacoraComunicacion.Tipo.novedad);
    }

    @Override
    @Transactional
    public void cambiarContrasena(Integer id, String nuevaContrasena) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado."));
        usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
        usuarioRepository.save(usuario);
        sesionRepository.invalidarSesionesPorUsuario(id);
        log.info("Contraseña actualizada para usuario: {} (ID: {})", usuario.getUsuario(), id);
        registrarBitacora("🔑 Contraseña actualizada para: " + usuario.getUsuario() + " (ID: " + id + ")",
                BitacoraComunicacion.Tipo.incidencia);
    }

    @Override
    @Transactional
    public void eliminarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado."));
        sesionRepository.invalidarSesionesPorUsuario(id);
        String nombre = usuario.getUsuario();
        usuarioRepository.deleteById(id);
        log.info("Usuario eliminado: {} (ID: {})", nombre, id);
        registrarBitacora("🗑️ Usuario eliminado: " + nombre + " (ID: " + id + ")",
                BitacoraComunicacion.Tipo.incidencia);
    }

    // ========== GESTIÓN DE SESIONES ==========

    /**
     * Cierra todas las sesiones de los vendedores activos (para cambio de turno).
     * Lee la configuración "horas_cierre_turno" para saber a qué horas debe ejecutarse.
     */
    @Override
    @Transactional
    public void cerrarSesionesPorTurno() {
        log.info("Ejecutando cierre de sesiones por cambio de turno");

        // Obtener horas de cierre desde configuración (ej: "13,18")
        String horasStr = configuracionService.getValor("horas_cierre_turno");
        List<Integer> horasCierre = parseHoras(horasStr);

        // Si no hay configuración, usar valores por defecto
        if (horasCierre.isEmpty()) {
            horasCierre = Arrays.asList(13, 18);
            log.warn("No se encontró configuración de horas de cierre, usando valores por defecto: 13,18");
        }

        // Obtener la hora actual
        int horaActual = java.time.LocalDateTime.now().getHour();

        // Solo ejecutar si la hora actual coincide con una de las configuradas
        if (!horasCierre.contains(horaActual)) {
            log.debug("No es hora de cierre de turno (hora actual: {}). Horas configuradas: {}", horaActual, horasCierre);
            return;
        }

        // Cerrar sesiones de todos los vendedores activos
        List<Usuario> vendedores = usuarioRepository.findByRolNombreAndActivoTrue("VENDEDOR");
        int contador = 0;
        for (Usuario v : vendedores) {
            sesionRepository.invalidarSesionesPorUsuario(v.getId());
            contador++;
        }
        log.info("Cierre de sesiones por turno completado: {} sesiones cerradas a las {}:00", contador, horaActual);
        if (contador > 0) {
            registrarBitacora("🔄 Cierre automático de " + contador + " sesiones por cambio de turno (hora " + horaActual + ":00)",
                    BitacoraComunicacion.Tipo.novedad);
        }
    }

    /**
     * Cierra todas las sesiones de un usuario específico (por ID).
     */
    @Override
    @Transactional
    public void cerrarSesionesPorUsuario(Integer usuarioId) {
        sesionRepository.invalidarSesionesPorUsuario(usuarioId);
        log.info("Sesiones cerradas para usuario ID: {}", usuarioId);
    }

    // ========== MÉTODO AUXILIAR ==========

    private List<Integer> parseHoras(String horasStr) {
        if (horasStr == null || horasStr.trim().isEmpty()) {
            return Arrays.asList();
        }
        try {
            return Arrays.stream(horasStr.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            log.warn("Formato de horas de cierre inválido: {}, usando valores por defecto", horasStr);
            return Arrays.asList();
        }
    }

    // ========== BITÁCORA ==========

    private void registrarBitacora(String mensaje, BitacoraComunicacion.Tipo tipo) {
        try {
            Integer usuarioId = securityUtils.getUsuarioIdAutenticado();
            com.example.proyecto.app.dto.request.BitacoraComunicacionRequest request =
                    com.example.proyecto.app.dto.request.BitacoraComunicacionRequest.builder()
                            .usuarioId(usuarioId)
                            .mensaje(mensaje)
                            .tipo(tipo)
                            .build();
            bitacoraService.crearMensaje(request);
        } catch (Exception e) {
            log.error("Error al registrar en bitácora: {}", e.getMessage());
        }
    }

    // ========== VALIDACIÓN DE INACTIVIDAD (opcional) ==========

    /**
     * Verifica si una sesión ha expirado por inactividad.
     * Lee la configuración "tiempo_inactividad_minutos".
     */
    public boolean isSesionExpiradaPorInactividad(String token) {
        // Buscar la sesión
        var sesion = sesionRepository.findByTokenAndActivaTrue(token);
        if (sesion.isEmpty()) {
            return true;
        }

        // Obtener tiempo de inactividad desde configuración
        Integer minutos = configuracionService.getValorInteger("tiempo_inactividad_minutos");
        if (minutos == null) {
            minutos = 30; // valor por defecto
        }

        // Calcular límite de inactividad
        var ultimaActividad = sesion.get().getUltimaActividad();
        if (ultimaActividad == null) {
            return true; // si no tiene registro, se considera expirada
        }

        return ultimaActividad.plusMinutes(minutos).isBefore(java.time.LocalDateTime.now());
    }
}