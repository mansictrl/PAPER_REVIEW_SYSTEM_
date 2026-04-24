// src/main/java/com/project/paperreview/config/SecurityConfig.java
package com.project.paperreview.config;

import com.project.paperreview.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authenticationProvider(authenticationProvider())

            .authorizeHttpRequests(auth -> auth

                // PUBLIC
                .requestMatchers("/", "/login.html", "/index.html", "/register.html",
                                  "/register/student", "/css/**", "/js/**")
                .permitAll()

                // STUDENT only
                .requestMatchers("/student.html", "/query.html", "/view/marks")
                .hasAuthority("STUDENT")

                // TEACHER only (dashboard page)
                .requestMatchers("/teacher.html")
                .hasAuthority("TEACHER")

                // TEACHER or ADMIN (shared functional pages)
                .requestMatchers("/marksEntry.html", "/viewQueries.html", "/updateMarks.html")
                .hasAnyAuthority("TEACHER", "ADMIN")

                // ADMIN only (pages)
                .requestMatchers("/admin.html", "/admin-report.html",
                                  "/registerTeacher.html", "/manage-teachers.html")
                .hasAuthority("ADMIN")

                // ADMIN only (APIs)
                .requestMatchers("/admin/**",
                                  "/register/teacher",
                                  "/register/teacher/**")
                .hasAuthority("ADMIN")

                // TEACHER or ADMIN — marks APIs
                .requestMatchers("/marks/all")
                .hasAnyAuthority("TEACHER", "ADMIN")

                // TEACHER or ADMIN — query APIs (including DELETE)
                .requestMatchers("/query/**")
                .hasAnyAuthority("STUDENT", "TEACHER", "ADMIN")

                // TEACHER or ADMIN — teacher info
                .requestMatchers("/teacher/**")
                .hasAnyAuthority("TEACHER", "ADMIN")

                // Everything else requires login
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .successHandler((request, response, authentication) -> {
                    String role = authentication.getAuthorities().iterator().next().getAuthority();
                    switch (role) {
                        case "TEACHER": response.sendRedirect("/teacher.html"); break;
                        case "STUDENT": response.sendRedirect("/student.html"); break;
                        case "ADMIN":   response.sendRedirect("/admin.html");   break;
                        default:        response.sendRedirect("/");
                    }
                })
                .failureUrl("/login.html?error=true")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login.html")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userDetailsService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}