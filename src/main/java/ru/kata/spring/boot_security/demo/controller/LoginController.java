package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.Collection;

@Controller
public class LoginController {

    private final UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/admin";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {

        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password. Please try again.");
        }

        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }

        return "login";
    }

    @GetMapping("/success")
    public String successLogin(Authentication authentication) {
        if (authentication != null && authentication.getAuthorities() != null) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

            for (GrantedAuthority authority : authorities) {
                if (authority.getAuthority().equals("ROLE_ADMIN")) {
                    return "redirect:/admin";
                }
            }
        }
        return "redirect:/user";
    }

    @GetMapping("/admin")
    public String admin(Authentication authentication, Model model) {
        // Добавляем currentUser в модель
        if (authentication != null) {
            String email = authentication.getName();
            User currentUser = userService.getUserByEmail(email);
            model.addAttribute("currentUser", currentUser);
        }

        // Также добавляем список пользователей для таблицы
        model.addAttribute("users", userService.getAllUsers());

        return "admin";
    }

    @GetMapping("/user")
    public String user(Authentication authentication, Model model) {
        // Добавляем currentUser в модель
        if (authentication != null) {
            String email = authentication.getName();
            User currentUser = userService.getUserByEmail(email);
            model.addAttribute("currentUser", currentUser);
        }
        return "user";
    }
}