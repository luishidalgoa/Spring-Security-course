package com.eazybytes.config;

import com.eazybytes.model.Customer;
import com.eazybytes.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio personalizado de autenticación de usuarios.
 *
 * Esta clase implementa la interfaz UserDetailsService de Spring Security para
 * personalizar la carga de datos de los usuarios desde la base de datos en lugar
 * de utilizar el UserDetailsService por defecto de Spring. Aquí, se define cómo
 * se obtienen los detalles de cada usuario y sus roles, necesarios para la autenticación
 * y autorización en el sistema de seguridad.
 */
@Service
@RequiredArgsConstructor
public class EazyBankUserDetailsService implements UserDetailsService {
    /**
     * Repositorio de clientes para acceder a los datos de usuario en la base de datos.
     *
     * Esta dependencia es inyectada automáticamente por Spring debido a la anotación
     * @RequiredArgsConstructor. Este repositorio permite buscar clientes por email,
     * el cual estamos usando como nombre de usuario en este caso.
     */
    private final CustomerRepository customerRepository;
    /*
     * Equivalente a usar el constructor manual y la anotación @Autowired para inyectar
     * el repositorio de cliente. @RequiredArgsConstructor elimina la necesidad de escribir
     * un constructor explícito, inyectando todas las propiedades finales de la clase.
     *
     * Ejemplo de constructor equivalente:
     *
     * @Autowired
     * private final CustomerRepository customerRepository;
     * public EazyBankUserDetailsService(CustomerRepository customerRepository) {
     *     this.customerRepository = customerRepository;
     * }
     */

    /**
     * Método para cargar un usuario por su nombre de usuario (en este caso, email).
     *
     * @param username el nombre de usuario que identifica al usuario cuyos datos
     *                 se desean cargar. En este caso, es el email del cliente.
     * @return UserDetails que contiene el nombre de usuario, contraseña y roles del usuario.
     * @throws UsernameNotFoundException si no se encuentra el usuario en la base de datos.
     *
     * Este método busca un usuario en la base de datos usando el email como nombre de usuario.
     * Si se encuentra el usuario, se crean las autoridades (roles) y se devuelve un objeto User
     * con los detalles necesarios para la autenticación. Si no se encuentra, lanza una excepción
     * UsernameNotFoundException.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscar al usuario en la base de datos por email
        Customer customer = customerRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User details not found for the user: " + username));

        // Crear lista de roles del usuario como GrantedAuthority
                //VIEJA FORMA
                //List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(customer.getRole()));
        List<GrantedAuthority> authorities =
                /*
                    Se puede hacer con foreach pero esta es una forma mas limpia de devolver una iteracion de
                    todos los roles convertidos en un objeto SimpleGrantedAuthority
                 */
                customer.getAuthorities().stream().map(
                        authority -> new SimpleGrantedAuthority(authority.getName())).collect(Collectors.toList()
                );

        // Retornar el usuario con su nombre de usuario (email), contraseña y roles
        return new User(customer.getEmail(), customer.getPwd(), authorities);
    }
}

