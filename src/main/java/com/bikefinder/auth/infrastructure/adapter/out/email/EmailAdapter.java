package com.bikefinder.auth.infrastructure.adapter.out.email;

import com.bikefinder.auth.application.port.output.EmailPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailAdapter implements EmailPort {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void sendVerificationEmail(String to, String fullName, String token) {
        String subject = "Verificá tu email - Bike Ecosystem";
        String verificationUrl = frontendUrl + "/auth/verify-email?token=" + token;
        String body = """
                Hola %s,
                
                Gracias por registrarte en Bike Ecosystem.
                Para verificar tu email hacé click en el siguiente link:
                
                %s
                
                Este link expira en 24 horas.
                
                Si no creaste una cuenta, ignorá este email.
                """.formatted(fullName != null ? fullName : "Usuario", verificationUrl);

        sendEmail(to, subject, body);
        log.info("Email de verificación enviado a: {}", to);
    }

    @Override
    public void sendPasswordResetEmail(String to, String fullName, String token) {
        String subject = "Resetear contraseña - Bike Ecosystem";
        String resetUrl = frontendUrl + "/auth/reset-password?token=" + token;
        String body = """
                Hola %s,
                
                Recibimos una solicitud para resetear tu contraseña.
                Hacé click en el siguiente link para continuar:
                
                %s
                
                Este link expira en 30 minutos.
                
                Si no solicitaste este cambio, ignorá este email.
                """.formatted(fullName != null ? fullName : "Usuario", resetUrl);

        sendEmail(to, subject, body);
        log.info("Email de reset de password enviado a: {}", to);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error enviando email a {}: {}", to, e.getMessage());
            throw new RuntimeException("Error al enviar email", e);
        }
    }
}
