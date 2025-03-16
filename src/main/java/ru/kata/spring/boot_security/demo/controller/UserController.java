package ru.kata.spring.boot_security.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {

    @GetMapping("/user")
    public String userPage(Model model, Authentication authentication) {
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
        }
        return "user";
    }

}