package com.github.zigcat.BlogPlatform.security.config;

import com.github.zigcat.BlogPlatform.services.DatabaseUserDetailsService;
import com.github.zigcat.BlogPlatform.security.SimpleLoggingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Autowired
    private DatabaseUserDetailsService userDetailsService;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(withDefaults())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/user/id/**").permitAll()
                        .requestMatchers("/api/user").permitAll()
                        .requestMatchers("/api/user/register").permitAll()
                        .requestMatchers("/api/post/id/**").permitAll()
                        .requestMatchers("/api/post/user/**").permitAll()
                        .requestMatchers("/api/post").permitAll()
                        .requestMatchers("/api/comment/id/**").permitAll()
                        .requestMatchers("/api/comment/post/**").permitAll()
                        .requestMatchers("/api/comment").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new SimpleLoggingFilter(), BasicAuthenticationFilter.class);
        return http.build();
    }
}
