package com.example.plantumlwebeditorv2.security;


import com.example.plantumlwebeditorv2.security.jwt.AuthEntryPointJwt;
import com.example.plantumlwebeditorv2.security.jwt.AuthTokenFilter;
import com.example.plantumlwebeditorv2.security.services.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

/*
 * JWT tokens for stateless authentication
 * BCrypt for password hashing
 * CORS configured for dev
 * PlantUML endpoints are public
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;

    // This runs on every request to check for valid JWT tokens in the Authorization header
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }


    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Default BCrypt password encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(httpSecurityCorsConfigurer ->
                        httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
                // Disabled CSRF for REST API -  using JWT tokens instead
                .csrf(AbstractHttpConfigurer::disable)
//                .exceptionHandling((exceptionHandling) ->
//                        exceptionHandling
//                                .accessDeniedPage("/errors/access-denied")
//                )
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(unauthorizedHandler)
                )
//                .sessionManagement((sessionManagement) ->
//                        sessionManagement
//                                .sessionConcurrency((sessionConcurrency) ->
//                                        sessionConcurrency
//                                                .maximumSessions(1)
//                                                .expiredUrl("/login?expired")))
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // URL-based security rules
                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                    // Authentication endpoints - always public
                    .requestMatchers("/api/auth/**").permitAll()

                    // PlantUML rendering endpoints - kept public for demo
                    .requestMatchers("/api/plantuml/render").permitAll()
                    .requestMatchers("/api/plantuml/image").permitAll()

                    // Static resources for React frontend
                    .requestMatchers("/", "/index.html", "/static/**", "/*.ico", "/*.json", "/*.png").permitAll()

                    // Everything else needs authentication
                    .anyRequest().authenticated());

        http.authenticationProvider(authenticationProvider());

        // Add JWT filter before the standard username/password filter
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS config for cross-origin requests
    // * origin only for dev

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // This needs changing eventually - Keep * for now
        configuration.setAllowedOrigins(List.of("*"));

        // Allow all common HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Headers commonly used by frontend framework
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setExposedHeaders(List.of("x-auth-token"));


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}

