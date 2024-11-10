package dev.luishidalgoa.springsection7.config;

import dev.luishidalgoa.springsection7.exceptionhandling.CustomAccessDeniedHandler;
import dev.luishidalgoa.springsection7.exceptionhandling.CustomBasicAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@Profile("prod") //esta clase se activara siempre y cuando el entorno de ejecución no sea prod
public class ProjectSecurityProdConfig {

    /**
     * NOTA:
     * UN BEAN es como un servicio inyectado que tiene su propio ciclo de vida. por lo que cuando
     * spring security haga su magia, en la lista de procesos se llamaran a los Beans segun hayan sido invocados o no y se
     * les requiera
     */

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        //http.authorizeHttpRequests((requests) -> requests.anyRequest().permitAll()
        //http.authorizeHttpRequests((request) -> request.anyRequest().denyAll() //no sera posible acceder
        //http.requiresChannel(rcc -> rcc.anyRequest().requiresSecure()); solo acepta peticiones HTTPS
        http
                .sessionManagement(smc -> smc.invalidSessionUrl("/invalidSession")// si la sesion de usuario es invalida o ha caducado se redirigira a la url indicada
                        .maximumSessions(3)// maximas sesiones permitidas por el mismo usuario
                        .maxSessionsPreventsLogin(true)) //tendra prioridad la sesion mas antigua sobre la nueva (por defecto tiene preferencia la mas nueva)
                .requiresChannel(rcc -> rcc.anyRequest().requiresSecure()) // Solo acepta peticiones HTTPS
                .csrf(csrfConfig -> csrfConfig.disable())
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/myAccount", "/myBalance", "/myCards", "/myLoans").authenticated() // rutas protegidas
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
