package com.eazybytes.controller;

import com.eazybytes.constants.ApplicationConstants;
import com.eazybytes.model.Customer;
import com.eazybytes.model.LoginRequestDTO;
import com.eazybytes.model.LoginResponseDTO;
import com.eazybytes.repository.CustomerRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final Environment env;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Customer customer) {
        try {
            String hashPwd = passwordEncoder.encode(customer.getPwd());
            customer.setPwd(hashPwd);
            customer.setCreateDt(new Date(System.currentTimeMillis()));
            Customer savedCustomer = customerRepository.save(customer);

            if (savedCustomer.getId() > 0) {
                return ResponseEntity.status(HttpStatus.CREATED).
                        body("Given user details are successfully registered");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                        body("User registration failed");
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                    body("An exception occurred: " + ex.getMessage());
        }
    }

    @RequestMapping("/user")
    public Customer getUserDetailsAfterLogin(Authentication authentication) {
        Optional<Customer> optionalCustomer = customerRepository.findByEmail(authentication.getName());
        return optionalCustomer.orElse(null);
    }


    /**
     * ES POSIBLE QUE ESTE METODO TE PAREZCA PARECIDO AL filtro JWTTokenValidatorFilter. Y es identico. Pero
     * el proposito de sustituir el filtro por este endpoint es para poder tener distintas formas de autenticar
     * @param loginRequest
     * @return
     */
    @PostMapping("/apiLogin")
    public ResponseEntity<LoginResponseDTO> apiLogin (@RequestBody LoginRequestDTO loginRequest) {
        // Variable para almacenar el JWT que se generará al autenticar al usuario
        String jwt = "";

        // Crear un objeto de autenticación no autenticado usando el nombre de usuario y la contraseña proporcionados
        // El método 'unauthenticated' indica que el usuario aún no ha sido autenticado y se debe realizar el proceso de autenticación
        Authentication authentication = UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.username(), loginRequest.password());

        // Autenticar el objeto 'authentication' usando el AuthenticationManager
        // Este proceso intenta autenticar al usuario con los detalles proporcionados
        Authentication authenticationResponse = authenticationManager.authenticate(authentication);

        // Verificar si la autenticación fue exitosa
        if (null != authenticationResponse && authenticationResponse.isAuthenticated()) {
            // Si la autenticación fue exitosa, generar el JWT
            if (null != env) {
                // Obtener la clave secreta desde el archivo de configuración (application.properties o application.yml)
                String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY, ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);

                // Crear una clave secreta utilizando la clave obtenida
                SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

                // Generar el JWT con las siguientes propiedades:
                // - Issuer (emisor): El nombre de la entidad que emite el token
                // - Subject (sujeto): El propósito del token
                // - Claims: Los datos personalizados que se añaden al token (nombre de usuario y roles)
                // - Expiration: Fecha de expiración del token (en este caso, 30000000 ms desde la creación)
                jwt = Jwts.builder()
                        .issuer("Eazy Bank") // Quien emite el token
                        .subject("JWT Token") // El propósito del token
                        .claim("username", authenticationResponse.getName()) // Añadir el nombre de usuario
                        .claim("authorities", authenticationResponse.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority) // Extraer los roles del usuario
                                .collect(Collectors.joining(","))) // Concatenar roles como una cadena
                        .issuedAt(new java.util.Date()) // Fecha de emisión
                        .expiration(new java.util.Date((new java.util.Date()).getTime() + 30000000)) // Expiración del token
                        .signWith(secretKey) // Firmar el token con la clave secreta
                        .compact(); // Generar el token compactado
            }
        }

        // Retornar la respuesta con el JWT generado y un estado HTTP 200 (OK)
        // El JWT se incluye en el encabezado de la respuesta con la clave definida en ApplicationConstants.JWT_HEADER
        return ResponseEntity.status(HttpStatus.OK)
                //NOTA: En un caso real, el JWT se puede incluir en uno de los dos modos, o en el encabezado o en el body
                .header(ApplicationConstants.JWT_HEADER, jwt) // Incluir el JWT en los encabezados de la respuesta
                .body(new LoginResponseDTO(HttpStatus.OK.getReasonPhrase(), jwt)); // Cuerpo de la respuesta con el JWT
    }


}
