package com.eazybytes.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
public class CrsfCookieFilter extends OncePerRequestFilter {
    /**
     * Este metodo es es el punto de entrada donde Spring Security permite que el desarrollador
     * inserte lógica personalizada dentro del flujo de filtrado de Spring, permitiendo modificar o
     * agregar comportamientos adicionales antes de que el procesamiento de la solicitud llegue al
     * controlador o al siguiente filtro en la cadena.
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //obtiene el token de los atributos de la solicitud
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        //garantiza la creacion del token si aun no existe
        csrfToken.getToken();
        //la solicitud seguira el flujo
        filterChain.doFilter(request, response);
    }
}
