package com.project.shopapp.configurations;

import com.project.shopapp.filters.JwtTokenFilter;
import com.project.shopapp.models.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity(debug = true)
@EnableWebMvc
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;
    @Value("${api.prefix}")
    private String apiPrefix;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)  throws Exception{
        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(
                                String.format("%s/users/register", apiPrefix),
                                String.format("%s/users/login", apiPrefix)
                        )
                        .permitAll()

                        .requestMatchers(GET,
                                String.format("%s/roles**", apiPrefix)).permitAll()

                        .requestMatchers(GET,
                                String.format("%s/categories**", apiPrefix)).permitAll()

                        .requestMatchers(POST,
                                String.format("%s/categories/**", apiPrefix)).hasAnyAuthority(Role.STAFF_LETAN,Role.STAFF_BAOVE)

                        .requestMatchers(PUT,
                                String.format("%s/categories/**", apiPrefix)).hasAnyAuthority(Role.ADMIN)

                        .requestMatchers(DELETE,
                                String.format("%s/categories/**", apiPrefix)).hasAnyAuthority(Role.ADMIN)

                        .requestMatchers(GET,
                                String.format("%s/products**", apiPrefix)).permitAll()

                        .requestMatchers(GET,
                                String.format("%s/products/**", apiPrefix)).permitAll()

                        .requestMatchers(GET,
                                String.format("%s/products/images/*", apiPrefix)).permitAll()

                        .requestMatchers(POST,
                                String.format("%s/products**", apiPrefix)).hasAnyAuthority(Role.ADMIN)

                        .requestMatchers(PUT,
                                String.format("%s/products/**", apiPrefix)).hasAnyAuthority(Role.ADMIN)

                        .requestMatchers(DELETE,
                                String.format("%s/products/**", apiPrefix)).hasAnyAuthority(Role.ADMIN)

                        .requestMatchers(POST,
                                String.format("%s/orders**", apiPrefix)).hasAnyAuthority(Role.USER)

                        .requestMatchers(GET,
                                String.format("%s/orders/**", apiPrefix)).permitAll()

                        .requestMatchers(PUT,
                                String.format("%s/orders/**", apiPrefix)).hasAnyAuthority(Role.ADMIN)

                        .requestMatchers(DELETE,
                                String.format("%s/orders/**", apiPrefix)).hasAnyAuthority(Role.ADMIN)

                        .requestMatchers(POST,
                                String.format("%s/order_details/**", apiPrefix)).hasAnyAuthority(Role.USER,Role.ADMIN)

                        .requestMatchers(GET,
                                String.format("%s/order_details/**", apiPrefix)).permitAll()

                        .requestMatchers(PUT,
                                String.format("%s/order_details/**", apiPrefix)).hasAnyAuthority(Role.ADMIN)

                        .requestMatchers(DELETE,
                                String.format("%s/order_details/**", apiPrefix)).hasAnyAuthority(Role.ADMIN)

                        .anyRequest().authenticated());

        http.cors(new Customizer<CorsConfigurer<HttpSecurity>>() {
            @Override
            public void customize(CorsConfigurer<HttpSecurity> httpSecurityCorsConfigurer) {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of("*"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
                configuration.setExposedHeaders(List.of("x-auth-token"));
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                httpSecurityCorsConfigurer.configurationSource(source);
            }
        });

        return http.build();
    }
}
