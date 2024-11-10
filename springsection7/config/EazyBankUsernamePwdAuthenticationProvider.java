package dev.luishidalgoa.springsection7.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * AHORA ESTA CLASE SE EJECUTA SOLO EN EL ENTORNO DE EJECÚCULO DE LA APLICACIÓN.
 * LA DIFERENCIA ENTRE ESTA CLASE Y LA DE PRODUCCIÓN ES QUE
 * ESTA CLASE NO VALIDARA LAS CREDENCIALES DEL USUARIO
 */
@Component // esta anotación indica que la clase es un componente de Spring Security
@Profile("!prod") //esta clase se activara siempre y cuando el entorno de ejecución no sea prod
@RequiredArgsConstructor
public class EazyBankUsernamePwdAuthenticationProvider implements AuthenticationProvider {
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    /**
     *
     * @param authentication objeto de autenticación que contiene el nombre de usuario y la contraseña introducidas
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String pwd = authentication.getCredentials().toString();
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        //si la contraseña introducida es correcta, se devuelve un objeto de autenticación
        return new UsernamePasswordAuthenticationToken(username, pwd, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
