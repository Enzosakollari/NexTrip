package com.example.demo.Service;

import com.example.demo.Booking.Booking;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendVerificationEmail(String email, String verificationToken) {
        String subject = "Email Verification";
        String path = "/req/signup/verify";
        String message = "Click the button below to verify your email address.";
        sendEmail(email, verificationToken, subject, path, message);
    }

//    public void sendForgottenPasswordEmail(String email, String resetToken) {
//        String subject = "Reset Password";
//        String path = "/req/signup/reset-password";
//        String message = "Click the button below to reset your password.";
//        sendEmail(email, resetToken, subject, path, message);
//    }

    private void sendEmail(String email, String token, String subject, String path, String message) {
        try {
            String actionUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(path)
                    .queryParam("token", token)
                    .toUriString();

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

            FileSystemResource logo = new FileSystemResource(
                    new File("src/main/resources/static/Images/logo.png")
            );
            helper.addInline("logo", logo);

            mailSender.send(mimeMessage);
            System.out.println(" Email sent successfully to " + email);
            System.out.println(" Verification URL: " + actionUrl);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(" Failed to send email: " + e.getMessage());
        }
    }
    public void sendBusinessVerificationEmail(String email, String verificationToken) {
        String subject = "Business Email Verification";
        String path = "/req/business/verify";
        String message = "Click the button below to verify your business email address.";
        sendEmail(email, verificationToken, subject, path, message);
    }

    public void sendTicketEmail(Booking booking) {
        if (booking == null) {
            return;
        }

        String email = booking.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }

        String subject = "Your NextTrip Ticket";
        String viewTicketsUrl = buildAbsoluteUrl("/my-tickets");
        String content = buildTicketEmailContent(booking, viewTicketsUrl);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setFrom(from);
            helper.setText(content, true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to send ticket email: " + e.getMessage());
        }
    }

    private String buildAbsoluteUrl(String path) {
        try {
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(path)
                    .toUriString();
        } catch (Exception ex) {
            return path;
        }
    }

    private String buildTicketEmailContent(Booking booking, String viewTicketsUrl) {
        String template = loadTemplate("templates/ticket.html");
        if (template == null || template.isBlank()) {
            return buildTicketFallback(booking, viewTicketsUrl);
        }

        String ticketCode = booking.getId() != null ? "NT-" + booking.getId() : "NT-NEW";
        String packTitle = booking.getTravelPackage() != null ? safe(booking.getTravelPackage().getTitle()) : "Travel Pack";
        String destination = booking.getTravelPackage() != null ? safe(booking.getTravelPackage().getDestination()) : "TBD";
        String startDate = booking.getTravelPackage() != null && booking.getTravelPackage().getStartDate() != null
                ? booking.getTravelPackage().getStartDate().toString()
                : "TBD";
        String endDate = booking.getTravelPackage() != null && booking.getTravelPackage().getEndDate() != null
                ? booking.getTravelPackage().getEndDate().toString()
                : "TBD";
        String createdAt = booking.getCreatedAt() != null
                ? booking.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
                : "-";

        return template
                .replace("{{ticketCode}}", ticketCode)
                .replace("{{ticketId}}", safe(booking.getId()))
                .replace("{{fullName}}", safe(booking.getFullName()))
                .replace("{{email}}", safe(booking.getEmail()))
                .replace("{{phone}}", safe(booking.getPhone()))
                .replace("{{packTitle}}", packTitle)
                .replace("{{destination}}", destination)
                .replace("{{startDate}}", startDate)
                .replace("{{endDate}}", endDate)
                .replace("{{travelers}}", String.valueOf(booking.getTravelersCount()))
                .replace("{{status}}", booking.getStatus() != null ? booking.getStatus().name() : "CONFIRMED")
                .replace("{{createdAt}}", createdAt)
                .replace("{{viewTicketsUrl}}", viewTicketsUrl);
    }

    private String buildTicketFallback(Booking booking, String viewTicketsUrl) {
        String ticketCode = booking.getId() != null ? "NT-" + booking.getId() : "NT-NEW";
        String packTitle = booking.getTravelPackage() != null ? safe(booking.getTravelPackage().getTitle()) : "Travel Pack";
        return """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 24px auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 14px; background: #fff;">
                  <h2 style="margin:0 0 12px;">Your NextTrip Ticket</h2>
                  <p style="margin:0 0 16px;">Thanks for booking. Your ticket is confirmed.</p>
                  <div style="padding: 12px 16px; background: #f8fafc; border-radius: 10px; margin-bottom: 16px;">
                    <strong>Ticket Code:</strong> %s
                  </div>
                  <p style="margin:0 0 6px;"><strong>Package:</strong> %s</p>
                  <p style="margin:0 0 6px;"><strong>Name:</strong> %s</p>
                  <p style="margin:0 0 16px;"><strong>Travelers:</strong> %s</p>
                  <a href="%s" style="display:inline-block; padding:10px 16px; background:#2563eb; color:#fff; text-decoration:none; border-radius:10px;">View My Tickets</a>
                </div>
                """.formatted(
                ticketCode,
                packTitle,
                safe(booking.getFullName()),
                booking.getTravelersCount(),
                viewTicketsUrl
        );
    }

    private String loadTemplate(String resourcePath) {
        try {
            var resource = new ClassPathResource(resourcePath);
            try (var stream = resource.getInputStream()) {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception ex) {
            return null;
        }
    }

    private String safe(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }

}
