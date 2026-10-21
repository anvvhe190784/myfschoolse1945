package vn.edu.fpt.fschool.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  private static final Logger log = LoggerFactory.getLogger(EmailService.class);

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username:no-reply@fschool.edu.vn}")
  private String fromEmail;

  @Value(
      "${app.mail.logo-url:https://raw.githubusercontent.com/anvvhe190784/myfschoolse1945/main/backend/src/main/resources/mail/fpt_logo.png}")
  private String logoUrl;

  @Value(
      "${app.mail.hero-url:https://raw.githubusercontent.com/anvvhe190784/myfschoolse1945/main/backend/src/main/resources/mail/otp_illustration.png}")
  private String heroUrl;

  public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  /**
   * Gửi email thật chứa mã xác thực OTP 6 số cho người dùng theo mẫu giao diện hiện đại (bất đồng
   * bộ).
   */
  @Async
  @CircuitBreaker(name = "default", fallbackMethod = "sendOtpEmailFallback")
  @Retry(name = "default", fallbackMethod = "sendOtpEmailFallback")
  public void sendOtpEmail(String toEmail, String otp) {
    log.info("Bắt đầu gửi email xác thực OTP đến: {}", toEmail);

    if (mailSender == null) {
      log.warn("JavaMailSender chưa được cấu hình. Bỏ qua gửi email qua SMTP.");
      return;
    }

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail, "FPT Schools System");
      helper.setTo(toEmail);
      helper.setSubject("[FPT Schools] Verification Code - Password Reset");

      String plainText =
          """
          Dear Student / Parent,

          You have requested to reset your password. In order to proceed, please enter the verification code: %s in FPT Schools application.

          This code will expire 5 minutes after this email was sent.

          If this is not requested by you, please contact FPT Schools support immediately.

          Best Regards,
          FPT Schools System
          """
              .formatted(otp);

      String htmlTemplate =
          """
          <!DOCTYPE html>
          <html lang="en">
          <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>[FPT Schools] Verification Code - Password Reset</title>
          </head>
          <body style="margin:0;padding:0;background-color:#c0d4e5;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;">
            <table width="100%" border="0" cellpadding="0" cellspacing="0" style="background-color:#c0d4e5;padding:30px 15px;">
              <tr><td align="center">
                <table width="100%" border="0" cellpadding="0" cellspacing="0" style="max-width:540px;background-color:#ffffff;border-radius:28px;padding:36px 30px;">
                  <tr><td align="center" style="padding-bottom:22px;">
                    <img src="{LOGO_URL}" alt="FPT Education" style="height:48px;width:auto;display:block;border:0;">
                  </td></tr>
                  <tr><td align="center" style="padding-bottom:22px;">
                    <div style="background-color:#f8fafc;border:1px solid #f1f5f9;border-radius:18px;padding:12px;">
                      <img src="{HERO_URL}" alt="" style="max-width:250px;width:100%;height:auto;display:block;margin:0 auto;border:0;">
                    </div>
                  </td></tr>
                  <tr><td align="center" style="padding-bottom:20px;">
                    <h1 style="margin:0 0 10px 0;font-size:24px;font-weight:800;color:#0f172a;line-height:1.3;">Password reset <span style="color:#f58220;">request</span></h1>
                    <p style="margin:0 0 8px 0;font-size:13px;color:#334155;line-height:1.5;">Dear Student / Parent,</p>
                    <p style="margin:0;font-size:13px;color:#334155;line-height:1.6;">You have requested to reset your password. In order to proceed, please enter the verification code: <strong>{OTP}</strong> in FPT Schools application.</p>
                  </td></tr>
                  <tr><td align="center" style="padding-bottom:12px;">
                    <div style="background-color:#f58220;border-radius:18px;padding:22px 16px;text-align:center;">
                      <div style="font-size:11px;font-weight:700;letter-spacing:2px;color:#fff3e0;text-transform:uppercase;margin-bottom:6px;">Your verification code</div>
                      <div style="font-size:42px;font-weight:900;letter-spacing:10px;color:#ffffff;font-family:'Courier New',Courier,monospace;padding:8px 0;">{OTP}</div>
                    </div>
                  </td></tr>
                  <tr><td align="center" style="padding-bottom:22px;">
                    <p style="margin:0;font-size:12px;color:#94a3b8;">This code will expire 5 minutes after this email was sent.</p>
                  </td></tr>
                  <tr><td style="padding-bottom:24px;">
                    <table width="100%" border="0" cellpadding="0" cellspacing="0" style="background-color:#f8fafc;border-radius:16px;padding:16px;border:1px solid #f1f5f9;font-size:13px;color:#334155;">
                      <tr><td style="line-height:1.6;padding-bottom:8px;"><strong style="color:#0f172a;">Keep it private:</strong> never share this code with anyone, including administrators.</td></tr>
                      <tr><td style="line-height:1.6;"><strong style="color:#0f172a;">Not you?</strong> If you did not request this, please contact FPT Schools support immediately.</td></tr>
                    </table>
                  </td></tr>
                  <tr><td align="center" style="border-top:1px solid #f1f5f9;padding-top:18px;font-size:12px;color:#94a3b8;line-height:1.6;">
                    <div style="font-weight:700;color:#475569;margin-bottom:2px;">FPT Schools System</div>
                    <div>This is a computer-generated email. Please do not reply to this email.</div>
                    <div>Copyright &copy; FPT Education. All rights reserved.</div>
                  </td></tr>
                </table>
              </td></tr>
            </table>
          </body>
          </html>
          """;

      String htmlContent =
          htmlTemplate
              .replace("{OTP}", otp)
              .replace("{LOGO_URL}", logoUrl)
              .replace("{HERO_URL}", heroUrl);

      // Gửi cả bản plain text và HTML, không đính kèm file CID để không sinh chip inline ngoài inbox
      helper.setText(plainText, htmlContent);

      mailSender.send(message);
      log.info("Đã gửi thành công email xác thực OTP đến: {}", toEmail);
    } catch (Exception e) {
      throw new MailSendException(
          "Lỗi khi chuẩn bị hoặc gửi email OTP tới " + toEmail + ": " + e.getMessage(), e);
    }
  }

  /** Fallback khi @Retry thử lại hết số lần cho phép mà vẫn thất bại hoàn toàn. */
  @SuppressWarnings(
      "java:S1172") // Parameter 'otp' is required by Resilience4j @Retry fallbackMethod signature
  // contract
  public void sendOtpEmailFallback(String toEmail, String otp, Throwable t) {
    log.error(
        "Hệ thống gửi email OTP tới {} thất bại hoàn toàn sau các lần thử lại: {}",
        toEmail,
        t.getMessage());
    throw new MailSendException(
        "Máy chủ gửi email đang gặp sự cố. Vui lòng thử lại sau giây lát.", t);
  }
}
