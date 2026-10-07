package com.hrms.security;

import com.hrms.entity.AdminEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AdminUserDetailsService adminUserDetailsService;

    @Value("${jwt.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenExpiration;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    public AuthController(
            @Qualifier("adminAuthenticationManager") AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            AdminUserDetailsService adminUserDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.adminUserDetailsService = adminUserDetailsService;
        logger.info("AuthController initialized");
    }

    private ResponseCookie buildCookie(String name, String value, String path, long maxAgeMs) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path(path)
                .maxAge(Duration.ofMillis(maxAgeMs))
                .build();
    }

    private ResponseCookie clearCookie(String name, String path) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path(path)
                .maxAge(0)
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request,
                                   HttpServletResponse response) {

        String mobile   = request.get("mobile");
        String password = request.get("password");

        logger.info("Login attempt for mobile: {}", mobile);

        if (mobile == null || mobile.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Mobile number is required");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(mobile, password)
            );

            final UserDetails userDetails = adminUserDetailsService.loadUserByUsername(mobile);
            final String accessToken  = jwtUtil.generateToken(userDetails);
            final String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            response.addHeader(HttpHeaders.SET_COOKIE,
                    buildCookie("admin_token",   accessToken,  "/", accessTokenExpiration).toString());
            response.addHeader(HttpHeaders.SET_COOKIE,
                    buildCookie("refresh_token", refreshToken, "/", refreshTokenExpiration).toString());

            logger.info("Login successful for mobile: {}", mobile);

            AdminDetails adminDetails = (AdminDetails) userDetails;
            AdminEntity admin = adminDetails.getAdminEntity();

            List<String> modules = admin.getModuleAccess() != null
                    ? admin.getModuleAccess()
                    : List.of();

            logger.info("Admin {} has module access: {}", admin.getAdminId(), modules);

            return ResponseEntity.ok(Map.of(
                    "message",      "Login successful",
                    "mobile",       mobile,
                    "adminId",      adminDetails.getAdminId() != null ? adminDetails.getAdminId() : "",
                    "role",         adminDetails.getRole()    != null ? adminDetails.getRole()    : "",
                    "firstName",    admin.getAdminFirstName() != null ? admin.getAdminFirstName() : "",
                    "lastName",     admin.getAdminLastName()  != null ? admin.getAdminLastName()  : "",
                    "moduleAccess", modules
            ));

        } catch (BadCredentialsException e) {
            logger.warn("Login failed - invalid credentials for mobile: {}", mobile);
            return ResponseEntity.status(401).body("Invalid mobile or password");
        } catch (Exception e) {
            logger.error("Unexpected error during login for mobile: {}", mobile, e);
            return ResponseEntity.status(500).body("Authentication error - please try again later");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request,
                                     HttpServletResponse response) {

        String refreshToken = null;
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(401).body("Refresh token missing");
        }

        try {
            String mobile = jwtUtil.extractUsername(refreshToken);
            UserDetails userDetails = adminUserDetailsService.loadUserByUsername(mobile);

            if (!jwtUtil.validateToken(refreshToken, userDetails)) {
                return ResponseEntity.status(401).body("Refresh token invalid or expired");
            }

            String newAccessToken = jwtUtil.generateToken(userDetails);
            response.addHeader(HttpHeaders.SET_COOKIE,
                    buildCookie("admin_token", newAccessToken, "/", accessTokenExpiration).toString());

            return ResponseEntity.ok(Map.of("message", "Token refreshed"));

        } catch (Exception e) {
            logger.error("Error during token refresh", e);
            return ResponseEntity.status(401).body("Token refresh failed");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie("admin_token",   "/").toString());
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie("refresh_token", "/").toString());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}