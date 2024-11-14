package com.eazybytes.eazyschool.config;

import com.eazybytes.eazyschool.handler.CustomAuthenticationFailureHandler;
import com.eazybytes.eazyschool.handler.CustomAuthenticationSuccessHandler;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;

@Configuration
@RequiredArgsConstructor
public class ProjectSecurityConfig {

    private final CustomAuthenticationFailureHandler authenticationFailureHandler;
    private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {

        http.csrf((csrf) -> csrf.disable())
                .authorizeHttpRequests((requests) -> requests.requestMatchers("/dashboard").authenticated()
                        .requestMatchers("/", "/home", "/holidays/**", "/contact", "/saveMsg",
                                "/courses", "/about", "/assets/**","login").permitAll())
                .formLogin(flc->flc.loginPage("/login")// redirigiremos a la url de login si no hay credenciales.
                        .defaultSuccessUrl("/dashboard")//despues de iniciar sesion redirigiremos a dashboard
                        //.usernameParameter("usernameOtheParam"))  //permite que spring security espere otro parametro en el atributo name del input
                        //.passwordParameter("passwordOtherParam")//lo mismo que el usernameParameter
                        .failureUrl("/login?error=true")//redirigiremos a login si falla la autenticacion
                        .successHandler(authenticationSuccessHandler) //succesHandler nos permite especificar un handler personalizado que sucede como evento despues de la autenticacion. Un handler nos permite dar mas flexibilidad a lo que deseamos hacer en lugar de lo que nos permitiria el defaultSuccessUrl
                        .failureHandler(authenticationFailureHandler)//lo mismo que el successHandler pero para el fallo de la autenticacion

                ).logout(loc->loc.logoutSuccessUrl("/login?logout=true") //redirigiremos a login si cerramos sesion
                        .invalidateHttpSession(true)//le indicamos a spring security que invalidemos la session despues de cerrar sesion
                        .clearAuthentication(true) //le indicamos a spring security que limpie la autenticacion despues de cerrar sesion
                        .deleteCookies("JSESSIONID")//le indicamos a spring security que borre la cookie JSESSIONID despues de cerrar sesion
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername("user")
                .password("{noop}EazyBytes@12345").authorities("read").build();
        UserDetails admin = User.withUsername("admin")
                .password("{bcrypt}$2a$12$88.f6upbBvy0okEa7OfHFuorV29qeK.sVbB9VQ6J6dWM1bW6Qef8m")
                .authorities("admin").build();
        return new InMemoryUserDetailsManager(user, admin);
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
