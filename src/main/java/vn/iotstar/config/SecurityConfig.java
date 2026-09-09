package vn.iotstar.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import vn.iotstar.dto.ApiResponse;
import vn.iotstar.security.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final vn.iotstar.security.CustomOAuth2UserService oAuth2UserService;
    private final ObjectMapper objectMapper;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          vn.iotstar.security.CustomOAuth2UserService oAuth2UserService) {
        this.userDetailsService = userDetailsService;
        this.oAuth2UserService = oAuth2UserService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Bean
    public ObjectMapper objectMapper() {
        return this.objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                .requestMatchers(
                    "/", "/home", "/login", "/register", "/verify-otp/**",
                    "/forgot-password/**", "/reset-password/**",
                    "/assets/**", "/static/**", "/css/**", "/js/**", "/images/**", "/uploads/**",
                    "/oauth2/**", "/error"
                ).permitAll()
                .requestMatchers("/admin/**", "/api/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/categories/**", "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/categories/**", "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/categories/**", "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categories/**", "/api/products/**").hasRole("ADMIN")
                .requestMatchers("/profile/**", "/api/categories/**", "/api/products/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .oauth2Login(oauth -> oauth
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oAuth2UserService)
                )
                .defaultSuccessUrl("/home", false)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("wep-secret-remember-key-2026")
                .rememberMeParameter("remember")
                .tokenValiditySeconds(30 * 60)
                .userDetailsService(userDetailsService)
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
            );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) {
                response.sendRedirect(request.getContextPath() + "/admin/categories");
            } else {
                response.sendRedirect(request.getContextPath() + "/home");
            }
        };
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            String uri = request.getServletPath();
            String accept = request.getHeader("Accept");
            boolean isApi = uri.startsWith("/api/") || (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE));

            if (isApi) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
                ApiResponse<Void> apiResponse = ApiResponse.error(HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập để thực hiện thao tác này");
                response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
            } else {
                response.sendRedirect(request.getContextPath() + "/login");
            }
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            String uri = request.getServletPath();
            String accept = request.getHeader("Accept");
            boolean isApi = uri.startsWith("/api/") || (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE));

            if (isApi) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
                ApiResponse<Void> apiResponse = ApiResponse.error(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện thao tác này");
                response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này");
            }
        };
    }
}
