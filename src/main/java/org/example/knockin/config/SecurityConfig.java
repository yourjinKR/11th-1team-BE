package org.example.knockin.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.global.KnockInProps;
import org.example.knockin.global.auth.service.CustomOAuth2UserService;
import org.example.knockin.global.auth.filter.CustomOAuth2Filter;
import org.example.knockin.global.auth.filter.TokenAuthenticationFilter;
import org.example.knockin.global.auth.filter.TokenExceptionFilter;
import org.example.knockin.global.auth.handler.CustomAccessDeniedHandler;
import org.example.knockin.global.auth.handler.CustomAuthenticationEntryPoint;
import org.example.knockin.global.auth.util.TokenProvider;
import org.example.knockin.global.auth.handler.OAuth2FailureHandler;
import org.example.knockin.global.auth.handler.OAuth2SuccessHandler;
import org.example.knockin.global.auth.handler.SecurityErrorResponseWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final TokenProvider tokenProvider;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final SecurityErrorResponseWriter securityErrorResponseWriter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
        return httpSecurity.formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .logout(AbstractHttpConfigurer::disable)
                .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(customAuthenticationEntryPoint).accessDeniedHandler(customAccessDeniedHandler))
                .oauth2Login(oauth -> oauth.userInfoEndpoint(c -> c.userService(customOAuth2UserService)).successHandler(oAuth2SuccessHandler).failureHandler(oAuth2FailureHandler))
                .addFilterBefore(new TokenAuthenticationFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new TokenExceptionFilter(securityErrorResponseWriter), TokenAuthenticationFilter.class)
                .addFilterBefore(new CustomOAuth2Filter(clientRegistrationRepository, customOAuth2UserService, oAuth2SuccessHandler, oAuth2FailureHandler), OAuth2AuthorizationRequestRedirectFilter.class)
                .authorizeHttpRequests(request -> request.requestMatchers("/h2-console/**","/auth/success","/error","/login/**","/oauth2/**").permitAll().anyRequest().authenticated())
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(KnockInProps knockInProps) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(knockInProps.getCorsUrls());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
