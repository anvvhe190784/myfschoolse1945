package vn.edu.fpt.fschool.service;

import io.github.resilience4j.retry.annotation.Retry;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@fschool.edu.vn}")
    private String fromEmail;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Gửi email thật chứa mã xác thực OTP 6 số cho người dùng theo mẫu giao diện hiện đại (đồng bộ).
     */
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
            helper.setSubject("[FPT Schools] Mã xác thực OTP đặt lại mật khẩu của bạn");

            String htmlTemplate = """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>FPT Schools OTP</title>
                </head>
                <body style="margin: 0; padding: 0; background-color: #c0d4e5; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
                    <table width="100%" border="0" cellpadding="0" cellspacing="0" style="background-color: #c0d4e5; padding: 30px 15px;">
                        <tr>
                            <td align="center">
                                <table width="100%" border="0" cellpadding="0" cellspacing="0" style="max-width: 540px; background-color: #ffffff; border-radius: 28px; box-shadow: 0 10px 25px rgba(0,0,0,0.06); padding: 36px 30px;">
                                    <!-- FPT Logo -->
                                    <tr>
                                        <td align="center" style="padding-bottom: 22px;">
                                            <img src="cid:fptLogo" alt="FPT Education Logo" style="height: 48px; max-height: 48px; width: auto; display: block; border: 0;" />
                                        </td>
                                    </tr>
                                    <!-- Hero Illustration -->
                                    <tr>
                                        <td align="center" style="padding-bottom: 22px;">
                                            <div style="background-color: #f8fafc; border: 1px solid #f1f5f9; border-radius: 18px; padding: 12px; text-align: center;">
                                                <img src="cid:heroImage" alt="FPT Schools OTP Verification" style="max-width: 250px; width: 100%; height: auto; display: block; margin: 0 auto; border: 0;" />
                                            </div>
                                        </td>
                                    </tr>
                                    <!-- Headline -->
                                    <tr>
                                        <td align="center" style="padding-bottom: 20px;">
                                            <h1 style="margin: 0 0 8px 0; font-size: 24px; font-weight: 800; color: #0f172a; line-height: 1.3;">
                                                Mã OTP của bạn <span style="color: #f58220;">đã sẵn sàng!</span>
                                            </h1>
                                            <p style="margin: 0; font-size: 13px; color: #64748b; line-height: 1.5;">
                                                Khôi phục quyền truy cập vào tài khoản <strong>F-School</strong> của bạn
                                            </p>
                                        </td>
                                    </tr>
                                    <!-- OTP Box -->
                                    <tr>
                                        <td align="center" style="padding-bottom: 12px;">
                                            <div style="background: linear-gradient(135deg, #f58220 0%, #ff9800 100%); background-color: #f58220; border-radius: 18px; padding: 22px 16px; text-align: center; box-shadow: 0 8px 20px rgba(245, 130, 32, 0.25);">
                                                <div style="font-size: 11px; font-weight: 700; letter-spacing: 2px; color: #fff3e0; text-transform: uppercase; margin-bottom: 6px;">
                                                    MÃ XÁC THỰC CỦA BẠN (OTP)
                                                </div>
                                                <div style="font-size: 42px; font-weight: 900; letter-spacing: 10px; color: #ffffff; font-family: 'Courier New', Courier, monospace, sans-serif; padding: 8px 0; user-select: all; -webkit-user-select: all;">
                                                    {OTP}
                                                </div>
                                                <div style="font-size: 12px; color: #fff3e0; font-weight: 500; margin-top: 6px;">
                                                    Mã có hiệu lực trong vòng <strong>5 phút</strong>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                    <!-- Instruction Note -->
                                    <tr>
                                        <td align="center" style="padding-bottom: 22px;">
                                            <p style="margin: 0; font-size: 12px; color: #94a3b8; font-style: italic;">
                                                * Vui lòng nhập mã 6 chữ số này vào ứng dụng để tiếp tục đặt lại mật khẩu.
                                            </p>
                                        </td>
                                    </tr>
                                    <!-- Checklist Section -->
                                    <tr>
                                        <td style="padding-bottom: 24px;">
                                            <table width="100%" border="0" cellpadding="0" cellspacing="0" style="background-color: #f8fafc; border-radius: 16px; padding: 16px; border: 1px solid #f1f5f9; font-size: 13px; color: #334155;">
                                                <tr>
                                                    <td style="vertical-align: top; width: 24px; padding-bottom: 10px;">
                                                        <div style="width: 18px; height: 18px; background-color: #ffedd5; color: #ea580c; border-radius: 50%; text-align: center; line-height: 18px; font-weight: bold; font-size: 11px;">✓</div>
                                                    </td>
                                                    <td style="vertical-align: top; padding-bottom: 10px; line-height: 1.5;">
                                                        <strong style="color: #0f172a;">Bảo mật tài khoản:</strong> Tuyệt đối không chia sẻ mã này cho bất kỳ ai, kể cả quản trị viên.
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td style="vertical-align: top; width: 24px; padding-bottom: 10px;">
                                                        <div style="width: 18px; height: 18px; background-color: #ffedd5; color: #ea580c; border-radius: 50%; text-align: center; line-height: 18px; font-weight: bold; font-size: 11px;">✓</div>
                                                    </td>
                                                    <td style="vertical-align: top; padding-bottom: 10px; line-height: 1.5;">
                                                        <strong style="color: #0f172a;">Thời gian hiệu lực:</strong> Mã sẽ tự động hết hạn sau 5 phút kể từ lúc gửi.
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td style="vertical-align: top; width: 24px;">
                                                        <div style="width: 18px; height: 18px; background-color: #ffedd5; color: #ea580c; border-radius: 50%; text-align: center; line-height: 18px; font-weight: bold; font-size: 11px;">✓</div>
                                                    </td>
                                                    <td style="vertical-align: top; line-height: 1.5;">
                                                        <strong style="color: #0f172a;">Hỗ trợ sinh viên:</strong> Nếu bạn không yêu cầu mã này, hãy đổi mật khẩu ngay hoặc liên hệ IT F-School.
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                    <!-- Clean Footer -->
                                    <tr>
                                        <td align="center" style="border-top: 1px solid #f1f5f9; padding-top: 18px; font-size: 12px; color: #94a3b8; line-height: 1.6;">
                                            <div style="font-weight: 700; color: #475569; margin-bottom: 2px;">FPT Schools System</div>
                                            <div>© 2026 FPT Education. Tất cả các quyền được bảo lưu.</div>
                                            <div style="font-size: 11px; color: #cbd5e1; margin-top: 4px;">Đây là email tự động từ hệ thống. Vui lòng không trả lời thư này.</div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """;

            String htmlContent = htmlTemplate.replace("{OTP}", otp);
            helper.setText(htmlContent, true);

            // Gắn ảnh inline qua chuẩn Content-ID (CID)
            ClassPathResource logoResource = new ClassPathResource("mail/fpt_logo.png");
            if (logoResource.exists()) {
                helper.addInline("fptLogo", logoResource, "image/png");
            }

            ClassPathResource heroResource = new ClassPathResource("mail/otp_illustration.png");
            if (heroResource.exists()) {
                helper.addInline("heroImage", heroResource, "image/png");
            }

            mailSender.send(message);
            log.info("Đã gửi thành công email xác thực OTP đến: {}", toEmail);
        } catch (Exception e) {
            log.error("Lỗi khi chuẩn bị hoặc gửi email OTP tới {}: {}", toEmail, e.getMessage());
            throw (e instanceof RuntimeException re) ? re : new MailSendException("Lỗi gửi mail: " + e.getMessage(), e);
        }
    }

    /**
     * Fallback khi @Retry thử lại hết số lần cho phép mà vẫn thất bại hoàn toàn.
     */
    public void sendOtpEmailFallback(String toEmail, String otp, Throwable t) {
        log.error("Hệ thống gửi email OTP tới {} thất bại hoàn toàn sau các lần thử lại: {}", toEmail, t.getMessage());
        throw new MailSendException("Máy chủ gửi email đang gặp sự cố. Vui lòng thử lại sau giây lát.", t);
    }
}
