package vn.edu.fpt.fschool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.edu.fpt.fschool.service.EmailService;

@SpringBootTest
class FschoolBackendApplicationTests {

	@Autowired
	private EmailService emailService;

	@Test
	void contextLoads() {
	}

	@Test
	void testLoadEnvPopulatesSystemProperties() {
		FschoolBackendApplication.loadEnv();
		assertThat(System.getProperty("SPRING_MAIL_USERNAME")).isEqualTo("vananvu249@gmail.com");
		assertThat(System.getProperty("SPRING_MAIL_HOST")).isEqualTo("smtp.gmail.com");
	}

	@Test
	void testSendOtpEmailDoesNotThrow() {
		assertDoesNotThrow(() -> {
			emailService.sendOtpEmail("test@fpt.edu.vn", "859241");
		});
	}

	@Test
	void testEmailServiceNullMailSenderHandledGracefully() {
		EmailService localService = new EmailService(null);
		assertDoesNotThrow(() -> localService.sendOtpEmail("test@fpt.edu.vn", "123456"));
	}

	@Test
	void testSendOtpEmailFallbackThrowsMailSendException() {
		EmailService localService = new EmailService(null);
		org.junit.jupiter.api.Assertions.assertThrows(
			org.springframework.mail.MailSendException.class,
			() -> localService.sendOtpEmailFallback("test@fpt.edu.vn", "123456", new RuntimeException("SMTP offline"))
		);
	}
}
