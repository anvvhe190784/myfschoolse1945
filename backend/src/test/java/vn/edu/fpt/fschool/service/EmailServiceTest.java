package vn.edu.fpt.fschool.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  @Mock private JavaMailSender mailSender;

  @InjectMocks private EmailService emailService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(emailService, "fromEmail", "no-reply@fschool.edu.vn");
    ReflectionTestUtils.setField(emailService, "logoUrl", "https://example.com/logo.png");
    ReflectionTestUtils.setField(emailService, "heroUrl", "https://example.com/hero.png");
  }

  @Test
  @DisplayName(
      "sendOtpEmail sends 100% English email with verification code and no Vietnamese diacritics")
  void sendOtpEmail_EnglishContent_Success() throws Exception {
    MimeMessage mimeMessage = new MimeMessage((Session) null);
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

    emailService.sendOtpEmail("a@b.com", "420363");

    verify(mailSender).send(mimeMessage);

    assertEquals("[FPT Schools] Verification Code - Password Reset", mimeMessage.getSubject());

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    mimeMessage.writeTo(outputStream);
    String rawContent = outputStream.toString(StandardCharsets.UTF_8);

    StringBuilder bodyText = new StringBuilder();
    Object messageContent = mimeMessage.getContent();
    if (messageContent instanceof Multipart multipart) {
      for (int i = 0; i < multipart.getCount(); i++) {
        BodyPart bodyPart = multipart.getBodyPart(i);
        bodyText.append(bodyPart.getContent().toString()).append("\n");
      }
    } else if (messageContent != null) {
      bodyText.append(messageContent.toString());
    }

    String allContent = rawContent + "\n" + bodyText;

    assertTrue(allContent.contains("420363"), "Email content must contain the OTP code");

    Pattern vietnameseDiacriticsPattern = Pattern.compile("[À-ỹ]");
    assertFalse(
        vietnameseDiacriticsPattern.matcher(allContent).find(),
        "Email content must not contain any Vietnamese diacritics");
  }
}
