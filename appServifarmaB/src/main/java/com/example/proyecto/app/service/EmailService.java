package com.example.proyecto.app.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailService {

    @Autowired
    private ParametroSistemaService parametroService;

    /**
     * Crea un JavaMailSender con las credenciales guardadas en la base de datos
     * y con detección automática del proveedor SMTP.
     */
    private JavaMailSender createMailSender() {
        // 1. Obtener parámetros de la base de datos
        String host = parametroService.obtenerValorPorClave("smtp.host");
        String port = parametroService.obtenerValorPorClave("smtp.port");
        String username = parametroService.obtenerValorPorClave("smtp.username");
        String password = parametroService.obtenerValorPorClave("smtp.password");
        String auth = parametroService.obtenerValorPorClave("smtp.auth");
        String starttls = parametroService.obtenerValorPorClave("smtp.starttls");

        // Validar que los parámetros esenciales existan
        if (host == null || username == null || password == null) {
            throw new IllegalStateException("Configuración SMTP incompleta. Verifique los parámetros del sistema.");
        }

        // 2. Construir el JavaMailSender
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(Integer.parseInt(port != null ? port : "587"));
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        // 3. Configurar propiedades comunes
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", Boolean.parseBoolean(auth != null ? auth : "true"));
        props.put("mail.smtp.starttls.enable", Boolean.parseBoolean(starttls != null ? starttls : "true"));
        props.put("mail.debug", "false");

        // 4. DETECCIÓN AUTOMÁTICA DE PROVEEDOR
        boolean esGoogle = username.endsWith("@gmail.com") 
                           || username.endsWith("@googlemail.com")
                           || "smtp.gmail.com".equalsIgnoreCase(host);

        if (esGoogle) {
            // Configuración específica para Gmail (normal y Google Workspace)
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            
            // Si se usa puerto 465, activar SSL explícito
            if ("465".equals(port)) {
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            }
            System.out.println("✅ Configuración para Google SMTP aplicada");
        } else {
            // Configuración genérica para otros proveedores (Outlook, SendGrid, etc.)
            // Si el host es diferente, se usa el trust por defecto
            System.out.println("✅ Configuración genérica para SMTP aplicada");
        }

        return mailSender;
    }

    /**
     * Envía un correo con adjunto usando las credenciales de la empresa (desde la BD).
     * El remitente es el correo configurado en 'smtp.username'.
     * El destinatario es el cliente.
     */
    public void enviarBoleta(String destino, String asunto, String mensaje,
                             byte[] adjunto, String nombreAdjunto)
            throws MessagingException {

        JavaMailSender mailSender = createMailSender();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(destino);
        helper.setSubject(asunto);
        helper.setText(mensaje);

        // Remitente: el correo de la farmacia (desde parametros_sistema)
        String from = parametroService.obtenerValorPorClave("smtp.username");
        helper.setFrom(from);

        // Adjuntar archivo (si existe)
        if (adjunto != null && adjunto.length > 0) {
            helper.addAttachment(nombreAdjunto, new ByteArrayResource(adjunto));
        }

        mailSender.send(message);
    }

    // ============================================================
    // (Opcional) Método con Reply-To, si decides usarlo más adelante
    // ============================================================
    public void enviarBoletaConReplyTo(String destino, String asunto, String mensaje,
                                       byte[] adjunto, String nombreAdjunto,
                                       String emailVendedor, String nombreVendedor)
            throws MessagingException {

        JavaMailSender mailSender = createMailSender();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(destino);
        helper.setSubject(asunto);
        helper.setText(mensaje);

        String from = parametroService.obtenerValorPorClave("smtp.username");
        helper.setFrom(from);

        if (emailVendedor != null && !emailVendedor.isEmpty()) {
            helper.setReplyTo(emailVendedor);
        }

        if (adjunto != null && adjunto.length > 0) {
            helper.addAttachment(nombreAdjunto, new ByteArrayResource(adjunto));
        }

        mailSender.send(message);
    }
}