package com.eazybytes.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Clase `KeycloakRoleConverter` implementa la interfaz `Converter` para convertir
 * un objeto `Jwt` en una colección de objetos `GrantedAuthority` utilizados por Spring Security.
 *
 * El propósito de esta clase es extraer los roles definidos en la sección `realm_access`
 * de un token JWT emitido por Keycloak y mapearlos a un formato compatible con Spring Security.
 */
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    /**
     * Método principal que realiza la conversión del token JWT a una colección de roles
     * compatibles con Spring Security.
     *
     * @param source el objeto `Jwt` que contiene los datos del token JWT.
     * @return una colección de objetos `GrantedAuthority` que representan los roles del usuario.
     */
    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        // Extrae el mapa que contiene los roles del `realm_access` del token JWT
        Map<String, Object> realmAccess = (Map<String, Object>) source.getClaims().get("realm_access");

        // Si no hay roles o el `realm_access` es nulo, devuelve una lista vacía
        if (realmAccess == null || realmAccess.isEmpty()) {
            return new ArrayList<>();
        }

        // Extrae los roles, los convierte a formato GrantedAuthority y los recopila en una lista
        Collection<GrantedAuthority> returnValue = ((List<String>) realmAccess.get("roles"))
                .stream() // Convierte la lista de roles a un flujo
                .map(roleName -> "ROLE_" + roleName) // Agrega el prefijo "ROLE_" a cada rol
                .map(SimpleGrantedAuthority::new)   // Crea una instancia de `SimpleGrantedAuthority`
                .collect(Collectors.toList());      // Recopila el flujo en una lista
        /**
         * en el flujo anterior, el estamos obteniendo uno por uno los elementos del array "realmAccess y le estamos
         * agregando el prefijo "ROLE_" para que el Spring Security lo reconozca como rol.
         * Por cada elemento que en el anterior map hemos creado, lo vamos a encapsular en una nueva instancia de
         * SimpleGrantedAuthority, y por ultimo cada instancia de SimpleGrantedAuthority lo vamos a agregar en una lista
         */
        return returnValue; // Devuelve la lista de autoridades
    }
}
