package dev.luishidalgoa.spring_section6.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class ProjectSecurityConfig {

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
        http
                .csrf(csrfConfig -> csrfConfig.disable())
                .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/myAccount", "/myBalance", "/myCards", "/myLoans").authenticated() // rutas protegidas
                .requestMatchers("/notices","/contact","/error","/register").permitAll())
                .formLogin(withDefaults())
                .httpBasic(withDefaults());
        return http.build();
    }

    /**
     * Carga los usuarios de la base de datos
     * @return
    @Bean
    public UserDetailsService userDetailsService(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }
     YA NO LO USAMOS POR QUE ESTAMOS USANDO UN userDetailsService personalizado que es la clase EazyBankUserDetailsService
     */

    /**
     * Este bean permite encriptar la contraseña en distintos tipos. lo cual nos permite usar {noop} o {bcrypt} por ejemplo
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Consume el API de HaveIBeenPwned para verificar si la contraseña introducida esta comprometida
     * @return
     */
    @Bean
    public CompromisedPasswordChecker compromisedPasswordChecker() {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }
}
