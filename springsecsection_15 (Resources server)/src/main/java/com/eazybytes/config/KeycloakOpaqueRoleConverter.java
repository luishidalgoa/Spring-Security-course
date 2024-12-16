package com.eazybytes.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenAuthenticationConverter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeycloakOpaqueRoleConverter implements OpaqueTokenAuthenticationConverter {

    /**
     * Convierte un token introspectado y un principal autenticado en una instancia de
     * `Authentication` para ser utilizada en el contexto de seguridad de la aplicación.
     *
     * @param introspectedToken el token introspectado en formato String.
     *                          Este token ya ha sido validado por el servidor de autorización.
     * @param authenticatedPrincipal un principal autenticado (`OAuth2AuthenticatedPrincipal`)
     *                               que contiene atributos relacionados con la autenticación del usuario.
     * @return una instancia de `UsernamePasswordAuthenticationToken` que representa al usuario autenticado
     *         y sus roles asociados, los cuales serán utilizados en el sistema de seguridad de Spring.
     */
    @Override
    public Authentication convert(String introspectedToken, OAuth2AuthenticatedPrincipal authenticatedPrincipal) {
        String username = authenticatedPrincipal.getAttributes().get("preferred_username").toString();
        Map<String,Object> realmAccess = authenticatedPrincipal.getAttribute("realm_access");
        Collection<GrantedAuthority> roles = ((List<String>) realmAccess.get("roles"))
                .stream() // Convierte la lista de roles a un flujo
                .map(roleName -> "ROLE_" + roleName) // Agrega el prefijo "ROLE_" a cada rol
                .map(SimpleGrantedAuthority::new)   // Crea una instancia de `SimpleGrantedAuthority`
                .collect(Collectors.toList());      // Recopila el flujo en una lista
        return new UsernamePasswordAuthenticationToken((authenticatedPrincipal.getName()),null,roles); // Devuelve los datos del usuario autenticado
    }
}
