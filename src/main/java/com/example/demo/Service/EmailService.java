package com.example.demo.Service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendVerificationEmail(String email, String verificationToken) {
        String subject = "Email Verification";
        String path = "/req/signup/verify"; // Your existing verification endpoint
        String message = "Click the button below to verify your email address.";
        sendEmail(email, verificationToken, subject, path, message);
    }

    public void sendForgottenPasswordEmail(String email, String resetToken) {
        String subject = "Reset Password";
        String path = "/req/signup/reset-password"; // Your existing reset endpoint
        String message = "Click the button below to reset your password.";
        sendEmail(email, resetToken, subject, path, message);
    }

    private void sendEmail(String email, String token, String subject, String path, String message) {
        try {
            // This is the same link structure you had before with query parameters
            String actionUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(path)
                    .queryParam("token", token)
                    .toUriString();

            // Keep your current dark theme style but with the working link structure
            String content = String.format("""
                    <div style="font-family: 'Segoe UI', Helvetica, Arial, sans-serif; max-width: 600px; margin: 30px auto;
                                      padding: 25px 30px; border-radius: 10px; background-color: #121212;\s
                                      border: 1px solid #2a2a2a; box-shadow: 0 4px 10px rgba(0,0,0,0.3); color: #e4e4e4;">
                    
                          <!-- Logo -->
                          <div style="text-align: center; margin-bottom: 25px;">
                              <img src="cid:logo" alt="NextTrip Logo" style="max-width: 140px; border-radius: 8px;">
                          </div>
                    
                          <!-- Title -->
                          <h2 style="color: #ffffff; text-align: center; margin-bottom: 20px;">%s</h2>
                    
                          <!-- Message -->
                          <p style="font-size: 15px; color: #cccccc; line-height: 1.7; text-align: center;">%s</p>
                    
                          <!-- Button -->
                          <div style="text-align: center; margin: 35px 0;">
                              <a href="%s" style="text-decoration: none; font-size: 15px; background-color: #007bff;\s
                                                 color: #ffffff; padding: 12px 35px; border-radius: 6px;
                                                 display: inline-block; font-weight: 600;">Verify Email</a>
                          </div>
                    
                         
                          <!-- Divider -->
                          <hr style="border: none; border-top: 1px solid #333; margin: 30px 0;">
                    
                          <!-- Footer -->
                          <p style="font-size: 12px; color: #777; text-align: center;">
                              © 2025 <strong>NextTrip</strong>. All rights reserved.<br>
                              This is an automated message. Please do not reply.
                          </p>
                      </div>
                    """, subject, message, actionUrl, actionUrl);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject(subject);
            helper.setFrom(from);
            helper.setText(content, true);

            // Attach logo
            FileSystemResource logo = new FileSystemResource(
                    new File("src/main/resources/static/Images/logo.png")
            );
            helper.addInline("logo", logo);

            mailSender.send(mimeMessage);
            System.out.println(" Email sent successfully to " + email);
            System.out.println(" Verification URL: " + actionUrl); // Debug print

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(" Failed to send email: " + e.getMessage());
        }
    }
}