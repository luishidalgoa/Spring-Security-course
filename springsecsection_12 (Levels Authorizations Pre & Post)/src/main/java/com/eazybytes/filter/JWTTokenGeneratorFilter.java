package com.eazybytes.filter;

import com.eazybytes.constants.ApplicationConstants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Generador de token JWT. extiende de OncePerRequestFilter por que solo queremos que se ejecute una vez por request
 * para de este modo evitar que se generen varios tokens
 */
public class JWTTokenGeneratorFilter extends OncePerRequestFilter {

    /**
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null != authentication) {
            Environment env = getEnvironment();
            if (null != env) {
                String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY,
                        ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
                SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                String jwt = Jwts.builder()
                        .issuer("Eazy Bank") // Especifica quién emite el token
                        .subject("JWT Token") // Define el tema del token (puede ser el propósito o el usuario)
                        .claim("username", authentication.getName()) // Añade un "claim" con el nombre de usuario
                        .claim("authorities", authentication.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.joining(","))) // Otro "claim" con las autoridades del usuario
                        .issuedAt(new Date()) // Fecha de emisión del token
                        .expiration(new Date((new Date()).getTime() + 30000000)) // Fecha de expiración (en este caso, 30,000,000 ms después de la emisión)
                        .signWith(secretKey) // Firma el token con la clave secreta
                        .compact(); // Genera el token como una cadena compacta

                response.setHeader(ApplicationConstants.JWT_HEADER, jwt);
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Cuando devolvemos true, el filtro no se ejecuta. De este modo evitamos que se ejecute el filtro y genere varios tokens
     * innecesarios por cada respuesta. ya que solo queremos que se ejecute en caso de que el usuario no tenga token o
     * el token haya caducado
     * @param request
     * @return
     * @throws ServletException
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !request.getServletPath().equals("/user");//cuando la url de la peticion sea /user se genera el token
    }

}
