package com.example.demo.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private UriComponentsBuilder uriBuilder;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "from", "test@example.com");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Create a UriComponentsBuilder for testing
        uriBuilder = UriComponentsBuilder.fromUriString("http://localhost:8080");
    }

    @Test
    void testEmailServices() {
        // Since we can't easily mock the static ServletUriComponentsBuilder,
        // we'll test that all email services are at least calling the right methods
        // without actually sending emails

        try (MockedStatic<ServletUriComponentsBuilder> mockedStatic = mockStatic(ServletUriComponentsBuilder.class)) {
            // Mock the static method to return our test builder
            mockedStatic.when(ServletUriComponentsBuilder::fromCurrentContextPath)
                    .thenReturn(uriBuilder);

            // Test verification email
            emailService.sendVerificationEmail("user@example.com", "verification-token");

            // Test forgotten password email
//            emailService.sendForgottenPasswordEmail("user@example.com", "reset-token");

            // Test business verification email
            emailService.sendBusinessVerificationEmail("business@example.com", "business-token");

            // Verify mailSender.send was called 3 times (once for each email)
            verify(mailSender, times(3)).send(any(MimeMessage.class));
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }
}
