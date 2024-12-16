# ¿Que hemos hecho durante esta seccion?
## Eliminación
---
Se ha agregado la dependencia de OAUTH2.0 Resource Server

````xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
````
---
Hemos eliminado los ficheros de configuracion:
    - El userDetailsService (encargado de extraer la informacion de la base de datos del usuario buscado)
    - El provider de autenticacion (encargado de llamar al userDetailsService y gestionar el proceso autenticación)
---
Hemos eliminado el Bean de authenticationManager (encargado de gestionar la autenticación) en la clase ProjectSecurityConfig
````java
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        EazyBankProdUsernamePwdAuthenticationProvider authenticationProvider =
                new EazyBankProdUsernamePwdAuthenticationProvider(userDetailsService, passwordEncoder);
        ProviderManager providerManager = new ProviderManager(authenticationProvider);
        providerManager.setEraseCredentialsAfterAuthentication(false);
        return  providerManager;
    }
````
---
Hemos eliminado los filtros adicionales que habiamos creado anteriormente en la clase ProjectSecurityConfig
````java
        return http
                .addFilterBefore(new RequestValidationBeforeFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(new AuthoritiesLoggingAfterFilter(), BasicAuthenticationFilter.class)
                .addFilterAt(new AuthoritiesLoggingAtFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(new JWTTokenGeneratorFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(new JWTTokenValidatorFilter(), BasicAuthenticationFilter.class)
````
por ende hemos eliminado las clases de los filtros mencionados.
Esto se hace debido a que ahora el servidor actuara como un servidor de recursos de OAUTH2.0 por lo que no se encargara de la autenticación de los usuarios. y cedera esa responsabilidad al servidor de OAUTH2.0
---
Se ha eliminado el metodo register del controlador de usuarios
## Lo nuevo
- ### Tokens JWT
#### ApplicationProperties
En el applications properties hemos agregado la propiedad: `spring.security.oauth2.resourceserver.jwt.jwk-set-uri`.
Esta propiedad nos permite indicarle al servidor de recursos de OAUTH2.0 que la informacion de los tokens va a ser obtenida de la url indicada

---
#### KeycloakRoleConverter
En la clase KeyCloakRoleConverter hemos creado un conversor de roles, de modo que traduzca los roles que son de keycloak
para que springboot los interprete como roles de springboot.

#### ProjectSecurityConfig
Hemos agregado la en la configuracion del servidor de recursos de modo que llame al conversor que hemos creado anteriormente
````java
//Creamos una instancia para convertir las autoridades de Keycloak a autoridades de Spring
JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
//Convertiremos los roles de Keycloak a roles de Spring. Es util porque keycloak manejar las autoridades de manera diferente a spring. Por ejemplo Keycloak: realm_*, Spring: ROLE_*
jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
/**
    * Anteriormente habiamos convertido los roles de Keycloak a roles que spring pueda entender
    * Ahora le diremos al servidor de recursos que cuando recibamos un Token utilizara
    * el convertidor para adaptar las autoridades del token a unas que spring pueda entender
    *
    * NOTA: Al indicarle al `rsc-> rsc.jwt`. Le estaremos diciendo al servidor de recursos
    * que el formato de token de acceso va a ser JWT
 */
        http.oauth2ResourceServer(rsc -> rsc.jwt(jwtConfigure
                -> jwtConfigure.jwtAuthenticationConverter(jwtAuthenticationConverter)));
````

- ### Tokens opacos
Un token opaco es un tipo de token que no tiene ningun tipo de estrcutura de datos por lo que no esta codificado en Base64
como los tokens JWT. Este tipo de token tiene la particularidad de que los servidores de recursos no pueden autenticarlos
por lo que el token debera ser siempre autorizado por el propio servidor de autenticación, a diferencia del token JWT que
si puede ser autorizado por cualquier servidor de recursos de OAUTH2.0.

---
#### ApplicationProperties
En el ``applications.properties`` hemos agregado las propiedades:
- `spring.security.oauth2.resourceserver.opaquetoken.introspection-uri` // Envia el clientID y clientSecret para autenticase el resource server con el auth server
- `spring.security.oauth2.resourceserver.opaquetoken.client-id`
- `spring.security.oauth2.resourceserver.opaquetoken.client-secret`
Estas propiedades nos permiten hacerles solicitudes al servidor de autenticación y que nos devuelva si es valido o no ademas de la informacion del usuario

---
#### KeycloakOpaqueRoleConverter
````java
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
````

#### ProjectSecurityConfig
Hemos comentado el codigo que habiamos escrito para JWT.
````java
/*
http.oauth2ResourceServer(rsc -> rsc.jwt(jwtConfigure
                -> jwtConfigure.jwtAuthenticationConverter(jwtAuthenticationConverter)));
*/
````
y se agrego la siguiente linea en sustitución:
````java
/**
 * Configura el servidor de recursos (Resource Server) para manejar tokens opacos.
 * 1. Define el endpoint de introspección del servidor de autorización (Authorization Server),
 *    que será invocado para validar los tokens opacos recibidos en las solicitudes.
 * 2. Proporciona las credenciales (clientId y clientSecret) necesarias para autenticar
 *    las solicitudes al servidor de autorización durante la introspección.
 * 3. Establece un `AuthenticationConverter` personalizado (KeycloakOpaqueRoleConverter)
 *    para transformar la información del token en una instancia de Authentication
 *    con roles específicos.
 */
http.oauth2ResourceServer(rsc -> rsc.opaqueToken(otc -> otc.authenticationConverter(new KeycloakOpaqueRoleConverter())
        .introspectionUri(this.introspectionUri)
.introspectionClientCredentials(this.clientId, this.clientSecret)
));
````
### Inicio de sesion con POSTMAN (GRANT TYPE)
durante esta sección hemos iniciado sesion de dos distintas formas:

- Client credentials
> Se expone tanto el client_id como el secret_key, no habria ningun usuario autenticandose de por medio. Ideal para usar apis dentro de otras apis
- Authorization code
> Se autentica el usuario de forma individual. Se expone tanto el client_id como el secret_key, Ideal para apis que seran consumidas por aplicaciones propietarias
- Authorization code + PKCE
> Igual que el Autherization code pero con PKCE no se expone el secret_key, ideal para apis que seran consumidas por terceros

#### Autenticacion de clientID + clientSecret
Deberemos elejir en Authorization : `OAuth 2.0` y en Grant Type : `Client Credentials`
posteriormente en Keycloak navegaremos al enlace:
[http://localhost:8180/realms/eazybankdev/.well-known/openid-configuration](http://localhost:8180/realms/eazybankdev/.well-known/openid-configuration)
y obtendremos el ``token_endpoint``

| Campo                  | Valor                                                                  |
|------------------------|------------------------------------------------------------------------|
| Token Name             | AccessToken                                                            |
| Grant type             | `Client Credentials `                                                  |
| Access Token URL       | http://localhost:8180/realms/eazybankdev/protocol/openid-connect/token |
| Client ID              | eazybankapi                                                            |
| Client Secret          | m77HL9uPzNyqb5cdBPX2TmdhIPLTkta                                        |
| Scope                  | openid email                                                           |
| Client Authentication  | `Send client credentials in body `                                     |

#### Autenticacion de usuario
| Campo                  | Valor                                                                  |
|------------------------|------------------------------------------------------------------------|
| Token Name            | AccessToken                                                            |
| Grant type            | `Authorization Code    `                                               |
| Callback URL          |                                                                        |
| Authorize using browser | Activado                                                               |
| Auth URL              | http://localhost:8180/realms/eazybankdev/protocol/openid-connect/auth  |
| Access Token URL      | http://localhost:8180/realms/eazybankdev/protocol/openid-connect/token |
| Client ID             | eazybankclient                                                         |
| Client Secret         | m77HL9uPzNyqb5cdBPX2TmdhIPLTkta                                        |
| Scope                 | openid email                                                           |
| State                 |                                                                        |
| Client Authentication | `Send client credentials in body  `                                    |

#### Authorization code + PKCE
| Campo                   | Valor                                                                  |
|-------------------------|------------------------------------------------------------------------|
| Token Name              | AccessToken                                                            |
| Grant type              | `Authorization Code    `                                               |
| Callback URL            |                                                                        |
| Authorize using browser | Activado                                                               |
| Auth URL                | http://localhost:8180/realms/eazybankdev/protocol/openid-connect/auth  |
| Access Token URL        | http://localhost:8180/realms/eazybankdev/protocol/openid-connect/token |
| Client ID               | eazybankclient                                                         |
| Client Secret           |                                                                        |
| Scope                   | openid email                                                           |
| State                   |                                                                        |
| Client Authentication   | `Send client credentials in body  `                                    |
| Code Challenge Method   | SHA-256                                                                |