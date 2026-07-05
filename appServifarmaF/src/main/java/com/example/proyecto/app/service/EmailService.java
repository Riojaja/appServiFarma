package com.example.proyecto.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.core.io.ByteArrayResource;


@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCorreoConAdjunto(String destino, String asunto, String mensaje, byte[] adjunto, String nombreAdjunto)
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(destino);
        helper.setSubject(asunto);
        helper.setText(mensaje);
        helper.addAttachment(nombreAdjunto, new ByteArrayResource(adjunto));
        mailSender.send(message);
    }
}
