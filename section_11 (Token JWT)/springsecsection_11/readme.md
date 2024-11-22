# ¿Que hemos hecho durante esta seccion?

## Archivos modificados o creados
```
|-- src.main.java.com.eazybytes
|   |-- controller
|   |   |-- UserController.java
|   |   |   |-- apiLogin()
|   |-- config
|   |   |-- ProjectSecurityConfig.java
|   |   |   |-- authenticationManager()
|   |--constants
|   |   |-- ApplicationConstants.java
|   |--filter
|   |   |-- JWTTokenGeneratorFilter.java
|   |   |-- JWTTokenValidatorFilter.java
|   |-- model
|   |   |-- LoginRequestDTO.java
|   |   |-- LoginResponseDTO.java
```
### ProjectSecurityConfig.java
En la linea 38 hemos cambiado la politica del sessionCreationPolicy de ALWAYS a Stateless para de este modo dejar de 
guardar la sesion en las cookies del usuario y guardarla unicamente en el SecurityContextHolder del servidor
### JWTTokenGeneratorFilter.java
Este filtro sera el encargado de generar un token JWT. Pero solo se ejecutara cuando la url de la peticion sea /user
eso quiere decir que solo se generara el token cuando el usuario inicie sesion y no cuando se realice cualquier peticion
### JWTTokenValidatorFilter.java
Este filtro sera el encargado de validar el token JWT. Pero solo se ejecutara cuando la url de la peticion sea distinta de /user
eso quiere decir que solo se validara el token cuando el usuario envie el token mediante el header Authorization y no cuando se intente
iniciar sesion

### Constants ApplicationConstants.java
Este fichero en realidad es solo para el entorno de desarrollo ya que en el almacenaremos la clave de encriptacion del token JWT
por lo que en produccion sera importante que subamos este fichero ya que los hackers pueden obtener la clave y hacer
vulnerable nuestra aplicacion
---

# Frontend
Desde el lado del frontend hemos modificado el interceptor de autenticacion para que ahora se encarga de enviar
el token JWT en el encabezado, reemplazando el Basic Auth de la anterior versión