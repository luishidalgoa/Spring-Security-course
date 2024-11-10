package dev.luishidalgoa.springsection7.exceptionhandling;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.time.LocalDate;

public class CustomBasicAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        //enviamos un error a la cabecera
        response.setHeader("eazybank-error-reason", "Authentication failed");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        //enviamos el error al body

        LocalDate currentTimeStamp = LocalDate.now();
        String message = (authException !=null && authException.getMessage() !=null) ? authException.getMessage() : "Unauthorized";
        String path = request.getRequestURI();
        //construccion del json de respuesta
        response.setContentType("application/json");
        String jsonResponse =
                String.format("{\"tiemstamp\": \"%s\", \"status\": \"%s\", \"error\": \"%s\", \"message\": \"%s\", \"path\": \"%s\"}",
                        currentTimeStamp,HttpStatus.UNAUTHORIZED.value(),HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                        message,path);
        response.getWriter().write(jsonResponse);
    }
}