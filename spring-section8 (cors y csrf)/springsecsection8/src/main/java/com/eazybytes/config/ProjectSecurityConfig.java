package com.eazybytes.config;

import com.eazybytes.exceptionhandling.CustomAccessDeniedHandler;
import com.eazybytes.exceptionhandling.CustomBasicAuthenticationEntryPoint;
import com.eazybytes.filter.CrsfCookieFilter;
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
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@Profile("!prod")
public class ProjectSecurityConfig {
    /**
     * NOTA:
     * UN BEAN es como un servicio inyectado que tiene su propio ciclo de vida. por lo que cuando
     * spring security haga su magia, en la lista de procesos se llamaran a los Beans segun hayan sido invocados o no y se
     * les requiera
     */
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        //CSRF: SE DEBE CONFIGURAR EL HANDLER DE LAS PETICIONES PARA VALIDAR EL TOKEN CSRF EN LAS SOLICITUDES DE CRUD AL BACKEND (ir a la linea de csrfConf donde se utiliza este objeto
        CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler = new CsrfTokenRequestAttributeHandler();
        http.csrf(csrfConfig -> csrfConfig
                        //???
                        .csrfTokenRequestHandler(csrfTokenRequestAttributeHandler)
                        .ignoringRequestMatchers("/contact","/register") //omite la validacion csrf para las siguientes peticiones
                        // Configura el token CSRF en una cookie que ademas es accesible desde JavaScript para solicitudes AJAX
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                //???
                .sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                //???
                .securityContext(contextConfig -> contextConfig.requireExplicitSave(false));

        http.cors(corsConfig -> corsConfig.configurationSource(new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration corsConfiguration = new CorsConfiguration();
                corsConfiguration.setAllowedOrigins(Collections.singletonList("http://localhost:4200")); // Only allow requests from http://localhost:4200
                corsConfiguration.setAllowedMethods(Collections.singletonList("*")); // Allow all HTTP methods for example GET, POST, PUT, DELETE
                corsConfiguration.setAllowedHeaders(Collections.singletonList("*")); // Allow all headers for example X-Api-Key or Authorization
                corsConfiguration.setAllowCredentials(true);
                corsConfiguration.setMaxAge(3600L); // el navegador del cliente recordara la configuración de cors durante 1 hora
                return corsConfiguration;
            }
        }));
        http
                .sessionManagement((smc) -> {
                    smc.invalidSessionUrl("/invalidSession")// si la sesion de usuario es invalida o ha caducado se redirigira a la url indicada
                            .maximumSessions(3)
                            .maxSessionsPreventsLogin(true);// maximas sesiones permitidas por el mismo usuario
                })
                .requiresChannel(rcc -> rcc.anyRequest().requiresInsecure()) // Only HTTP
                .addFilterAfter(new CrsfCookieFilter(), BasicAuthenticationFilter.class)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/myAccount", "/myBalance", "/myLoans", "/myCards", "/user").authenticated()
                        .requestMatchers("/notices", "/contact", "/error", "/register", "/invalidSession").permitAll());
        http.formLogin(withDefaults());
        http.httpBasic(hbc -> hbc.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint()));
        http.exceptionHandling(ehc -> ehc.accessDeniedHandler(new CustomAccessDeniedHandler())); //cuando se genera una excepcion de acceso denegado se llama al handler customizado

        return http.build();
    }
    /**
     * Carga los usuarios de la base de datos
     * @return
     @Bean public UserDetailsService userDetailsService(DataSource dataSource) {
     return new JdbcUserDetailsManager(dataSource);
     }
     YA NO LO USAMOS POR QUE ESTAMOS USANDO UN userDetailsService personalizado que es la clase EazyBankUserDetailsService
     */

    /**
     * Este bean permite encriptar la contraseña en distintos tipos. lo cual nos permite usar {noop} o {bcrypt} por ejemplo
     *
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Consume el API de HaveIBeenPwned para verificar si la contraseña introducida esta comprometida
     *
     * @return
     */
    @Bean
    public CompromisedPasswordChecker compromisedPasswordChecker() {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }

}
