package com.hrms.security;

import com.hrms.config.BcryptEncoderConfig;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AdminUserDetailsService adminUserDetailsService;
    private final EmployeeUserDetailsService employeeUserDetailsService;


    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          AdminUserDetailsService adminUserDetailsService,
                          EmployeeUserDetailsService employeeUserDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.adminUserDetailsService = adminUserDetailsService;
        this.employeeUserDetailsService = employeeUserDetailsService;
        logger.info("SecurityConfig initialized");
    }

//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BcryptEncoderConfig();
    }



    // ── Admin pipeline ───────────────────────────────────────────────────────

    @Bean
    public DaoAuthenticationProvider adminAuthenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider(adminUserDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    /**
     * Admin AuthenticationManager built as a plain ProviderManager with NO parent.
     *
     * Root cause of the StackOverflowError we kept hitting:
     * AuthenticationConfiguration.getAuthenticationManager() returns a ProviderManager
     * whose parent is the DSL-built HttpSecurity manager. That DSL manager contains
     * the same providers we registered via .authenticationProvider(). So calling
     * authenticate() goes: our bean -> parent DSL manager -> delegates back to our bean
     * -> repeat until stack blows.
     *
     * Fix: build both managers ourselves with new ProviderManager(providers).
     * No AuthenticationConfiguration, no parent, no proxy loop.
     */
    @Bean
    @Primary
    public AuthenticationManager adminAuthenticationManager() {
        ProviderManager manager = new ProviderManager(List.of(adminAuthenticationProvider()));
        manager.setEraseCredentialsAfterAuthentication(false);
        logger.debug("Admin AuthenticationManager (plain ProviderManager) created");
        return manager;
    }

    // ── Employee pipeline ────────────────────────────────────────────────────

    @Bean
    public DaoAuthenticationProvider employeeAuthenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider(employeeUserDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean("employeeAuthenticationManager")
    public AuthenticationManager employeeAuthenticationManager() {
        ProviderManager manager = new ProviderManager(List.of(employeeAuthenticationProvider()));
        manager.setEraseCredentialsAfterAuthentication(false);
        logger.debug("Employee AuthenticationManager (plain ProviderManager) created");
        return manager;
    }

    // ── CORS ─────────────────────────────────────────────────────────────────

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "https://hrms.siecwater.com","https://employee.siecwater.com", "http://localhost:5500", "http://127.0.0.1:5500",
                "http://localhost:5501", "http://127.0.0.1:5501",
                "http://localhost:5502", "http://127.0.0.1:5502",
                "http://localhost:3000",
                "http://localhost:8080",
                "http://localhost:8086","http://localhost:5502"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // ── Security filter chain ─────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring SecurityFilterChain");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(f -> f.disable()))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/bootstrap").permitAll()
                        .requestMatchers("/api/admin/auth/**").permitAll()
                        .requestMatchers("/api/admin/**").authenticated()
                        .requestMatchers("/api/employee/auth/**").permitAll()
                        .requestMatchers("/api/employee/**").authenticated()
                        .anyRequest().permitAll()
                )

                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authenticationProvider(adminAuthenticationProvider())
                .authenticationProvider(employeeAuthenticationProvider())

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, e) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Unauthorized - Login required (JWT)\"}");
                    logger.warn("Unauthorized: {} — {}", request.getRequestURI(), e.getMessage());
                })
        );

        logger.info("SecurityFilterChain configured successfully");
        return http.build();
    }
}