package ru.kata.spring.boot_security.demo.configs;

import jakarta.servlet.ServletException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class SuccessUserHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Override
    protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        String targetUrl = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .anyMatch(role -> role.equals("ROLE_ADMIN")) ? "/admin" : "/user";

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
