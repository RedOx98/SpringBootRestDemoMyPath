package com.olahammed.SpringRestDemo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private RSAKey rsaKey;

    // private final RsaKeyProperties rsaKeys;

    // public SecurityConfig(RsaKeyProperties rsaKeys) {
    //     this.rsaKeys = rsaKeys;
    // }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        rsaKey = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (JWKSelector, securityContext) -> JWKSelector.select(jwkSet);
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // @Bean
    // public InMemoryUserDetailsManager users() {
    //     return new InMemoryUserDetailsManager(
    //         User.withUsername("olaide")
    //         .password("{noop}password")
    //         .authorities("read")
    //         .build()
    //     );
    // }

    @Bean
    public AuthenticationManager authManager(UserDetailsService userDetailsService) {
        var authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setUserDetailsService(userDetailsService);
        return new ProviderManager(authProvider);
    }

    @Bean
    JwtDecoder jwtDecoder() throws JOSEException {
        return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
    }

    @Bean
    JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwks) {
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
        .csrf(csrf -> 
        csrf
        .ignoringRequestMatchers("/db/console/**")
        )
        .headers(header ->
        header
        .frameOptions().sameOrigin()
        )
        .authorizeHttpRequests(
            authorizeRequests ->
            authorizeRequests
            .requestMatchers("/").permitAll()
            .requestMatchers("/api/v1/auth/token").permitAll()
            .requestMatchers("/api/v1/auth/users/add").permitAll()
            .requestMatchers("/api/v1/auth/users").hasAuthority("SCOPE_ADMIN")
            .requestMatchers("/api/v1/auth/users/{userId}/update-authorities").hasAuthority("SCOPE_ADMIN")
            .requestMatchers("/api/v1/auth/profile").authenticated()
            .requestMatchers("/api/v1/auth/profile/delete").authenticated()
            .requestMatchers("/api/v1/albums/add").authenticated()
            .requestMatchers("/api/v1/albums").authenticated()
            .requestMatchers("/api/v1/albums/{albumId}").authenticated()
            .requestMatchers("/api/v1/albums/{albumId}/update").authenticated()
            .requestMatchers("/api/v1/albums/{albumId}/photos/{photoId}/update").authenticated()
            .requestMatchers("/api/v1/albums/{albumId}/photos/{photoId}/delete").authenticated()
            .requestMatchers("/api/v1/albums/{albumId}/upload-photos").authenticated()
            .requestMatchers("/api/v1/albums/{albumId}/{photoId}/download-photo").permitAll()
            .requestMatchers("/api/v1/albums/{albumId}/{photoId}/download-thumbnail").permitAll()
            .requestMatchers("/api/v1/api/**").permitAll()
            .requestMatchers("/api/v1/auth/profile/update-password").permitAll()
            .requestMatchers("/swagger-ui/**").permitAll()
            .requestMatchers("/v3/api-docs/**").permitAll()

        )
        .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.csrf(csrf->
        csrf.disable()
        );

        http.headers(header->
        header.frameOptions().disable()
        );


        return http.build();

    }
}
