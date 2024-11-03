package dev.luishidalgoa.spring_section4.controller;

import dev.luishidalgoa.spring_section4.model.Customer;
import dev.luishidalgoa.spring_section4.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para registrar nuevos usuarios en la aplicación.
 *
 * Este controlador expone un endpoint para crear nuevos usuarios en el sistema.
 * Al implementar este flujo, aseguramos que las contraseñas se guarden de manera segura
 * mediante el uso de PasswordEncoder, compatible con Spring Security, y almacenamos
 * los usuarios en la base de datos usando CustomerRepository.
 *
 * El flujo general de Spring Security comienza con el registro de usuarios en este
 * controlador, seguido de la autenticación de usuarios registrados a través de la
 * configuración de seguridad.
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    /**
     * Repositorio de clientes para guardar y acceder a los datos del usuario en la base de datos.
     * Este repositorio permite interactuar con la base de datos para guardar nuevos usuarios.
     */
    private final CustomerRepository customerRepository;

    /**
     * Codificador de contraseñas para encriptar la contraseña del usuario antes de almacenarla.
     * PasswordEncoder es parte de Spring Security y proporciona métodos seguros de codificación.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Endpoint para registrar un nuevo usuario en el sistema.
     *
     * Este método se invoca cuando un cliente realiza una solicitud HTTP POST a "/register"
     * con los datos de usuario en el cuerpo de la solicitud. La contraseña se codifica
     * mediante PasswordEncoder antes de guardarla en la base de datos para garantizar la
     * seguridad. Si el usuario se guarda correctamente, devuelve un código de estado 201 (CREATED).
     *
     * @param customer Objeto Customer que contiene los datos del nuevo usuario, incluyendo
     *                 el nombre de usuario y la contraseña en texto claro.
     * @return ResponseEntity con un mensaje de éxito y código de estado 201 si el usuario
     *         se crea correctamente, 400 si ocurre un error en la creación y 500 en caso
     *         de una excepción.
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Customer customer) {
        try {
            // Codificar la contraseña del usuario
            String hashPwd = passwordEncoder.encode(customer.getPwd()); // Encriptamos la contraseña
            customer.setPwd(hashPwd); // Actualizar el objeto Customer con la contraseña encriptada

            // Guardar el nuevo usuario en la base de datos
            Customer result = customerRepository.save(customer);

            // Verificar que el usuario se haya guardado correctamente
            if (result.getId() >= 0) {
                return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully");
            }
        } catch (Exception e) {
            // Manejo de errores si ocurre una excepción al guardar el usuario
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An exception occurred: " + e.getMessage());
        }

        // Retornar un error 400 si el usuario no se creó por alguna razón desconocida
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not created");
    }
}

