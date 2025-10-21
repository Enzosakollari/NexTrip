package com.example.demo.Service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendVerificationEmail(String email,String verificationToken){
        String subject="Email Verification";
        String path = "req/signup/verify";
        String message = "Click the button below to verify your email adress";
        sendEmail(email,verificationToken,subject,path,message);

    }

    public void sendForgottenPasswordEmail(String email,String resetToken){
        String subject="Reset Password";
        String path = "req/signup/reset-password";
        String message = "Click the button below to reset your password";
        sendEmail(email,resetToken,subject,path,message);
    }


    private void sendEmail(String email,String token,String subject,String path,String message){
        try {
            String actionUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(path)
                    .queryParam("token", token)
                    .toUriString();
            String content = """
                    <div style="font-family: 'Segoe UI', Helvetica, Arial, sans-serif; max-width: 600px; margin: 30px auto;
                                padding: 25px 30px; border-radius: 10px; background-color: #f8f9fa; 
                                border: 1px solid #e0e0e0; box-shadow: 0 2px 5px rgba(0,0,0,0.05);">

                        <h2 style="color: #1a1a1a; text-align: center; margin-bottom: 20px;">%s</h2>

                        <p style="font-size: 15px; color: #333; line-height: 1.6;">%s</p>

                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="text-decoration: none; font-size: 15px; background-color: #007bff; 
                                               color: #ffffff; padding: 12px 25px; border-radius: 6px;
                                               display: inline-block;">Open Link</a>
                        </div>

                        <p style="font-size: 13px; color: #555; text-align: center;">
                            Or copy this link into your browser:
                        </p>
                        <p style="word-break: break-all; font-size: 13px; color: #0066cc; text-align: center;">%s</p>

                        <hr style="border: none; border-top: 1px solid #ddd; margin: 30px 0;">
                        <p style="font-size: 12px; color: #999; text-align: center;">
                            This is an automated message. Please do not reply.
                        </p>
                    </div>
                    """.formatted(subject, message, actionUrl, actionUrl);
        MimeMessage mimeMessage =mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setFrom(from);
            helper.setText(content,true);
            mailSender.send(mimeMessage);
        }catch (Exception e){
            System.err.println("Failed to senf email:"+e.getMessage());
        }
    }
}
