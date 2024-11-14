package dev.luishidalgoa.springsection7.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component // esta anotación indica que la clase es un componente de Spring Security
@Slf4j // esta anotación indica que la clase usa el log de Spring para registrar eventos
public class AuthenticationEvents {
    /**
     * Automaticamente cuando un usuario inicia sesion correctamente, se llama a este metodo
     * @param event objeto con la informacion de la autenticacion
     */
    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        log.info("Inicio de sesion satisfactori: {}", event.getAuthentication().getName());
    }
    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failureEvent) {
        log.error("Error de autenticacion: {} - {}", failureEvent.getAuthentication().getName(), failureEvent.getException().getMessage());
    }

}
