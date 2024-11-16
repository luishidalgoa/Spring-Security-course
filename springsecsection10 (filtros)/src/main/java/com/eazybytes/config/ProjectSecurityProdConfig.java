package com.eazybytes.config;

import com.eazybytes.exceptionhandling.CustomAccessDeniedHandler;
import com.eazybytes.exceptionhandling.CustomBasicAuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@Profile("prod")
public class ProjectSecurityProdConfig {
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        //CSRF: SE DEBE CONFIGURAR EL HANDLER DE LAS PETICIONES PARA VALIDAR EL TOKEN CSRF EN LAS SOLICITUDES DE CRUD AL BACKEND (ir a la linea de csrfConf donde se utiliza este objeto
        CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler = new CsrfTokenRequestAttributeHandler();
        http.csrf(csrfConfig -> csrfConfig.
                        csrfTokenRequestHandler(csrfTokenRequestAttributeHandler)
                        .ignoringRequestMatchers("/contact","/register")//omite la validacion csrf para las siguientes peticiones
                        // Configura el token CSRF en una cookie que ademas es accesible desde JavaScript para solicitudes AJAX
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                //???
                .sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                //???
                .securityContext(contextConfig -> contextConfig.requireExplicitSave(false));

        http.cors(corsConfig -> corsConfig.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration corsConfiguration = new CorsConfiguration();
                        corsConfiguration.setAllowedOrigins(Collections.singletonList("https://localhost:4200")); // Only allow requests from http://localhost:4200
                        corsConfiguration.setAllowedMethods(Collections.singletonList("*")); // Allow all HTTP methods
                        corsConfiguration.setAllowedHeaders(Collections.singletonList("*")); // Allow all headers for example X-Api-Key or Authorization
                        corsConfiguration.setAllowCredentials(true);
                        corsConfiguration.setMaxAge(3600L); // el navegador del cliente recordara la configuración de cors durante 1 hora
                        return corsConfiguration;
                    }
                }))
                .sessionManagement(smc -> smc.invalidSessionUrl("/invalidSession").maximumSessions(1).maxSessionsPreventsLogin(true))
                .requiresChannel(rcc -> rcc.anyRequest().requiresSecure()) // Only HTTPS
                .csrf(csrfConfig -> csrfConfig.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))// la cookie no estará restringida únicamente para el uso HTTP, permitiendo que JavaScript en el cliente pueda accederla. Esta configuración puede ser útil en aplicaciones donde se necesita que el front-end maneje directamente el token CSR
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/myAccount").hasAuthority("VIEWACCOUNT") //los usuarios con el rol podran acceder
                        .requestMatchers("/myBalance").hasAnyAuthority("VIEWBALANCE","VIEWACCOUNT") //los usuarios con alguno de los 2 roles podran acceder
                        .requestMatchers( "/myLoans").hasAuthority("VIEWLOANS")
                        .requestMatchers("/myCards").hasAuthority("VIEWCARDS")
                        .requestMatchers( "/user").authenticated()
                        .requestMatchers("/notices", "/contact", "/error", "/register", "/invalidSession").permitAll());
        http.formLogin(withDefaults());
        http.httpBasic(hbc -> hbc.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint()));
        http.exceptionHandling(ehc -> ehc.accessDeniedHandler(new CustomAccessDeniedHandler()));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * From Spring Security 6.3 version
     *
     * @return
     */
    @Bean
    public CompromisedPasswordChecker compromisedPasswordChecker() {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }

}
