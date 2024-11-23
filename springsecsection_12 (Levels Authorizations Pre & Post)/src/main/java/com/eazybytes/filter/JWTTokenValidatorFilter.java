package com.eazybytes.filter;

import com.eazybytes.constants.ApplicationConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JWTTokenValidatorFilter extends OncePerRequestFilter {
    /**
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
       String jwt = request.getHeader(ApplicationConstants.JWT_HEADER);
       if(null != jwt) {
           try {
               Environment env = getEnvironment();
               if (null != env) {
                   String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY,
                           ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
                   SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                   if(null !=secretKey) {
                       Claims claims = Jwts.parser()
                               .verifyWith(secretKey) // Verifica la firma del token usando la clave secreta
                               .build()
                               .parseSignedClaims(jwt) // Analiza el token JWT recibido (en la variable `jwt`)
                               .getPayload(); // Obtiene el payload del token
                       String username = String.valueOf(claims.get("username"));
                       String authorities = String.valueOf(claims.get("authorities"));
                       Authentication authentication = new UsernamePasswordAuthenticationToken(username, null,
                               AuthorityUtils.commaSeparatedStringToAuthorityList(authorities));
                       SecurityContextHolder.getContext().setAuthentication(authentication);
                   }
               }

           } catch (ExpiredJwtException expiredException) {
               // Lanza una excepción personalizada si el token ha expirado
               throw new BadCredentialsException("Token expired!", expiredException);
           } catch (Exception exception) {
               // Manejo genérico de excepciones para tokens inválidos
               throw new BadCredentialsException("Invalid Token received!", exception);
           }
       }
        filterChain.doFilter(request,response);
    }

    /**
     * Este filtro se activara en casa de que la url de la peticion no sea /user.
     * El motivo es por que este filtro su funcion es validar el token que previamente genero el filtro JWTTokenGeneratorFilter
     * @param request
     * @return
     * @throws ServletException
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getServletPath().equals("/user");
    }

}
