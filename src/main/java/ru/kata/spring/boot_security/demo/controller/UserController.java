package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.Optional;


@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String getUserPage(Model model) {
        // Получаем email текущего пользователя из SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();  // Это email, если используется Spring Security

        // Получаем пользователя по email
        Optional<User> user = userService.findByEmail(email);

        // Если пользователь найден, передаем его в модель
        if (user.isPresent()) {
            model.addAttribute("user", user.get()); // Передаем сам объект User
        } else {
            model.addAttribute("error", "Пользователь не найден.");
        }

        return "user";  // Возвращаем страницу user.html
    }
}
