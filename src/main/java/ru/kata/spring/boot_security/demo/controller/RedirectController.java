package ru.kata.spring.boot_security.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RedirectController {

    @GetMapping("/redirectByRole")
    public String redirectByRole(HttpServletRequest request) {
        if (request.isUserInRole("ADMIN")) {
            return "redirect:/admin";
        } else if (request.isUserInRole("USER")) {
            return "redirect:/user";
        }
        return "redirect:/login";
    }
}
