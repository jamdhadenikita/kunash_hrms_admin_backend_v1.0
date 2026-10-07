package com.hrms.service.serviceImpl;

import com.hrms.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ── Logo path inside src/main/resources ──────────────────────────────────
    // Kunash Media logo — dark background wala PNG
    private static final String LOGO_PATH = "static/Images/kunash-logo.png";

    // ── Brand Colors (Kunash Media theme) ────────────────────────────────────
    private static final String BRAND_ORANGE      = "#E87B35";  // Kunash K icon + "KUNASH" text
    private static final String BRAND_ORANGE_DARK = "#C4661F";  // hover / darker shade
    private static final String BRAND_GRAY        = "#4A4A4A";  // "MEDIA" text
    private static final String BRAND_DARK        = "#1a1a1a";  // header background
    private static final String BRAND_LIGHT_BG    = "#FFF6EE";  // very light orange tint
    private static final String BRAND_BORDER      = "#F5DFC8";  // subtle orange border

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ── Send OTP Email ────────────────────────────────────────────────────────

    @Override
    public void sendOtpEmail(String toEmail, String subject, String otp) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            // multipart = true  →  required for inline attachments (logo)
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(buildOtpEmailTemplate(otp), true);   // true = HTML

            // Attach Kunash Media logo as inline CID image
            addLogoIfExists(helper);

            mailSender.send(mimeMessage);
            logger.info("OTP email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }
    }

    // ── Attach logo as inline image (safe — won't crash if file missing) ─────

    private void addLogoIfExists(MimeMessageHelper helper) {
        try {
            ClassPathResource logoResource = new ClassPathResource(LOGO_PATH);
            if (logoResource.exists() && logoResource.isReadable()) {
                helper.addInline("kunash-logo", logoResource);   // CID = "kunash-logo"
                logger.info("Kunash Media logo attached successfully from: {}", LOGO_PATH);
            } else {
                logger.warn("Logo not found at '{}' — email will be sent without logo.", LOGO_PATH);
            }
        } catch (Exception e) {
            logger.warn("Could not attach logo to email: {} — email will be sent without logo.", e.getMessage());
        }
    }

    // ── HTML Email Template — OTP ────────────────────────────────────────────

    private String buildOtpEmailTemplate(String otp) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>Password Reset OTP – Kunash Media HRMS</title>" +
                "<style>" +
                "  body { margin:0; padding:0; font-family:'Segoe UI',Arial,sans-serif;" +
                "         background-color:#f4f4f4; }" +
                "  .wrapper { max-width:600px; margin:40px auto; background:#ffffff;" +
                "             border-radius:12px; overflow:hidden;" +
                "             box-shadow:0 4px 20px rgba(0,0,0,0.10); }" +
                "  .header { background:" + BRAND_DARK + "; text-align:center;" +
                "             padding:32px 30px 24px;" +
                "             border-bottom:4px solid " + BRAND_ORANGE + "; }" +
                "  .header img { max-height:70px; max-width:280px; object-fit:contain; }" +
                "  .header .fallback { font-size:22px; font-weight:800; color:" + BRAND_ORANGE + ";" +
                "                     letter-spacing:3px; display:none; }" +
                "  .body { padding:38px 40px 28px; color:#333333; }" +
                "  .body p { font-size:15px; line-height:1.75; margin:0 0 16px; }" +
                "  .otp-box { text-align:center; margin:28px 0; }" +
                "  .otp-code { display:inline-block; font-size:38px; font-weight:800;" +
                "              color:" + BRAND_ORANGE_DARK + "; background:" + BRAND_LIGHT_BG + ";" +
                "              border:3px solid " + BRAND_ORANGE + ";" +
                "              border-radius:10px; padding:16px 44px;" +
                "              letter-spacing:14px;" +
                "              font-family:'Courier New',monospace; }" +
                "  .hint { color:#888; font-size:13px; margin-top:10px; }" +
                "  .notice { background:#FFF8E1; border-left:4px solid " + BRAND_ORANGE + ";" +
                "            padding:14px 18px; border-radius:6px; margin:22px 0; }" +
                "  .notice ul { margin:6px 0 0 0; padding-left:18px;" +
                "               font-size:14px; color:#555; line-height:1.9; }" +
                "  .footer { background:#f8fafc; text-align:center;" +
                "            padding:20px; font-size:12px; color:#999;" +
                "            border-top:1px solid #e5e7eb; }" +
                "  .footer .brand { color:" + BRAND_ORANGE + "; font-weight:600; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='wrapper'>" +

                // ── HEADER: dark bg + Kunash Media logo via CID ─────────────
                "  <div class='header'>" +
                "    <img src='cid:kunash-logo' alt='Kunash Media'" +
                "         onerror=\"this.style.display='none';" +
                "                  document.querySelector('.fallback').style.display='block';\" />" +
                "    <div class='fallback'>KUNASH MEDIA</div>" +
                "  </div>" +

                // ── BODY ─────────────────────────────────────────────────────
                "  <div class='body'>" +
                "    <p>Hello,</p>" +
                "    <p>We received a request to reset your <strong>Kunash Media HRMS</strong> portal password." +
                "       Please use the OTP below to proceed.</p>" +
                "    <p>If you did not request this, please ignore this email or" +
                "       contact your HR administrator immediately.</p>" +

                "    <div class='otp-box'>" +
                "      <div class='otp-code'>" + otp + "</div>" +
                "      <div class='hint'>Select and copy the OTP above</div>" +
                "    </div>" +

                "    <div class='notice'>" +
                "      <strong>&#9888; Important:</strong>" +
                "      <ul>" +
                "        <li>This OTP is valid for <strong>5 minutes</strong> only.</li>" +
                "        <li>Do <strong>not</strong> share this code with anyone.</li>" +
                "        <li>If you did not request a password reset, contact your HR admin immediately.</li>" +
                "      </ul>" +
                "    </div>" +

                "    <p>Regards,<br><strong>Kunash Media Support Team</strong><br>" +
                "    <span style='color:#888;font-size:13px;'>" +
                "    Human Resource Management System</span></p>" +
                "  </div>" +

                // ── FOOTER ───────────────────────────────────────────────────
                "  <div class='footer'>" +
                "    <p>&copy; 2026 <span class='brand'>KUNASH MEDIA</span>. All rights reserved.</p>" +
                "    <p>This is an automated email — please do not reply.</p>" +
                "  </div>" +

                "</div>" +
                "</body>" +
                "</html>";
    }

    // ── Send Welcome Credentials Email ───────────────────────────────────────

    @Override
    public void sendEmployeeCredentials(String toEmail, String employeeId, String password) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to Kunash Media HRMS - Your Login Credentials");

            helper.setText(buildWelcomeCredentialsTemplate(employeeId, password), true); // HTML

            addLogoIfExists(helper);

            mailSender.send(mimeMessage);
            logger.info("Welcome credentials email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send credentials email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send credentials email: " + e.getMessage());
        }
    }

    private String buildWelcomeCredentialsTemplate(String employeeId, String password) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>Welcome to Kunash Media HRMS</title>" +
                "<style>" +
                "  body { margin:0; padding:0; font-family:'Segoe UI',Arial,sans-serif; background-color:#f4f4f4; }" +
                "  .wrapper { max-width:600px; margin:40px auto; background:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 4px 20px rgba(0,0,0,0.10); }" +
                "  .header { background:" + BRAND_DARK + "; text-align:center; padding:32px 30px 24px; border-bottom:4px solid " + BRAND_ORANGE + "; }" +
                "  .header img { max-height:70px; max-width:280px; object-fit:contain; }" +
                "  .header .fallback { font-size:22px; font-weight:800; color:" + BRAND_ORANGE + "; letter-spacing:3px; display:none; }" +
                "  .body { padding:38px 40px 28px; color:#333333; }" +
                "  .body p { font-size:15px; line-height:1.75; margin:0 0 16px; }" +
                "  .credential-box { background:" + BRAND_LIGHT_BG + "; border:2px solid " + BRAND_ORANGE + "; border-radius:10px; padding:20px; margin:25px 0; text-align:center; }" +
                "  .label { font-size:14px; color:#555; margin-bottom:5px; }" +
                "  .value { font-size:18px; font-weight:700; color:" + BRAND_ORANGE_DARK + "; letter-spacing:1px; font-family:'Courier New',monospace; }" +
                "  .portal-btn { display:inline-block; background:" + BRAND_ORANGE + "; color:#ffffff; text-decoration:none; " +
                "               padding:14px 28px; border-radius:8px; font-weight:600; margin:20px 0; font-size:16px; }" +
                "  .notice { background:#FFF8E1; border-left:4px solid " + BRAND_ORANGE + "; padding:14px 18px; border-radius:6px; margin:22px 0; font-size:14px; color:#555; }" +
                "  .footer { background:#f8fafc; text-align:center; padding:20px; font-size:12px; color:#999; border-top:1px solid #e5e7eb; }" +
                "  .footer .brand { color:" + BRAND_ORANGE + "; font-weight:600; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='wrapper'>" +

                // Header with Kunash Media Logo
                "  <div class='header'>" +
                "    <img src='cid:kunash-logo' alt='Kunash Media'" +
                "         onerror=\"this.style.display='none'; document.querySelector('.fallback').style.display='block';\" />" +
                "    <div class='fallback'>KUNASH MEDIA</div>" +
                "  </div>" +

                "  <div class='body'>" +
                "    <p>Hello,</p>" +
                "    <p>Welcome to the <strong>Kunash Media HRMS Employee Portal</strong>! Your account has been successfully created.</p>" +

                "    <div class='credential-box'>" +
                "      <div class='label'>Employee ID</div>" +
                "      <div class='value'>" + employeeId + "</div>" +
                "      <br>" +
                "      <div class='label'>Temporary Password</div>" +
                "      <div class='value'>" + password + "</div>" +
                "    </div>" +

                "    <p style='text-align:center; margin:30px 0 20px 0;'>" +
                "      <a href='https://employee.kunashshowcase.online/index.html' class='portal-btn' target='_blank'>" +
                "        Visit Employee Portal Login" +
                "      </a>" +
                "    </p>" +

                "    <p>You can now login using the credentials above.</p>" +
                "    <p><strong>We strongly recommend changing your password after your first login for security.</strong></p>" +

                "    <div class='notice'>" +
                "      <strong>Important:</strong> Keep your credentials secure. Do not share your password with anyone." +
                "    </div>" +

                "    <p>Regards,<br><strong>Kunash Media Support Team</strong><br>" +
                "    <span style='color:#888;font-size:13px;'>" +
                "    Human Resource Management System</span></p>" +
                "  </div>" +

                "  <div class='footer'>" +
                "    <p>&copy; 2026 <span class='brand'>KUNASH MEDIA</span>. All rights reserved.</p>" +
                "    <p>This is an automated email — please do not reply.</p>" +
                "  </div>" +

                "</div>" +
                "</body>" +
                "</html>";
    }
}