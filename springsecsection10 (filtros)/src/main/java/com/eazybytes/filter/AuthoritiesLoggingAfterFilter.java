package com.eazybytes.filter;

import jakarta.servlet.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
@Slf4j
public class AuthoritiesLoggingAfterFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null!=authentication) {
            log.info("User " + authentication.getName() + " is successfully authenticated and"+
                    " has the authorities:" + authentication.getAuthorities().toString());
        }
        chain.doFilter(request, response); //devuelve la respuesta al siguiente filtro
    }
}
