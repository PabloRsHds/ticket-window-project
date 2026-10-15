package br.com.queue.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${public.key}")
    private String publicKey;

    @Value("${private.key}")
    private String privateKey;

    @Value("${app.frontend.url}")
    private String frontEndUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .cors(cors -> cors.configurationSource(request -> {

                    var configuration = new CorsConfiguration();

                    configuration.setAllowedOrigins(List.of(
                            frontEndUrl
                    ));

                    configuration.setAllowedMethods(List.of(
                            "GET",
                            "POST",
                            "PUT",
                            "DELETE",
                            "PATCH",
                            "OPTIONS"
                    ));

                    configuration.setAllowedHeaders(List.of(
                            "Content-Type",
                            "Authorization"
                    ));

                    configuration.setAllowCredentials(true);

                    return configuration;
                }))
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/login/**").permitAll()
                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/v3/api-docs/**").permitAll()
                                .requestMatchers("/ws/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/units").permitAll()
                                .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public RSAPublicKey publicKey() throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKey);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(keyBytes);

        return (RSAPublicKey) keyFactory.generatePublic(spec);
    }

    @Bean
    public RSAPrivateKey privateKey() throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKey);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PKCS8EncodedKeySpec spec =
                new PKCS8EncodedKeySpec(keyBytes);

        return (RSAPrivateKey) keyFactory.generatePrivate(spec);
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {

        return web -> web.ignoring().requestMatchers("/ws/**");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder jwtEncoder(
            RSAPublicKey publicKey,
            RSAPrivateKey privateKey
    ) {

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .build();

        JWKSource<SecurityContext> jwks =
                new ImmutableJWKSet<>(new JWKSet(rsaKey));

        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey publicKey) {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }
}
