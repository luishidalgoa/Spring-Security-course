package dev.luishidalgoa.spring_section3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
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
        http.authorizeHttpRequests((requests) -> requests
                .requestMatchers("/myAccount", "/myBalance", "/myCards", "/myLoans").authenticated() // rutas protegidas
                .requestMatchers("/notices","/contact","/error").permitAll())
                .formLogin(withDefaults())
                .httpBasic(withDefaults());
        return http.build();
    }

    /**
     * Crea una lista de usuarios en memoria para la autenticación
     * @return
     */
    @Bean
    public UserDetailsService userDetailsService() {
        //usamos noop para decirle a spring que deseo manejar la contrasena en texto plano. Si no se usa, nos arrojara spring una excepcion
        UserDetails user = User.withUsername("user").password("{noop}luishidalgoa1").authorities("read").build();
        UserDetails admin = User.withUsername("admin").password("{bcrypt}$2a$12$heUjDTAnZF8ee3YVPCtSj.ch0iD5Doh/VzqTAU.Qeq7yo431Ago7e")
                .authorities("read", "write").build();//la clave se encripta con bcrypt, la clave es: 12345
        return new InMemoryUserDetailsManager(user,admin);
    }

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
