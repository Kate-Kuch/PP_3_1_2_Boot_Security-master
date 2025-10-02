package ru.kata.spring.boot_security.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.*;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;

    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public String adminPanel(Model model, Authentication authentication) {
        String email = authentication.getName();
        Optional<User> currentUser = userService.findByEmail(email);

        if (currentUser.isPresent()) {
            model.addAttribute("currentUser", currentUser.get());
        } else {
            return "redirect:/error";
        }

        List<User> allUsers = userService.findAll();
        model.addAttribute("users", allUsers);

        // Добавляем список всех ролей для формы
        List<Role> allRoles = roleService.findAll();
        model.addAttribute("allRoles", allRoles);

        return "admin";
    }

    @PostMapping("/create")
    public String createUser(@RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam int age,
                             @RequestParam String email,
                             @RequestParam String password,
                             @RequestParam List<String> roles) {

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAge(age);
        user.setEmail(email);
        user.setPassword(password);

        userService.createUser(user, roles);
        return "redirect:/admin";
    }

    @PostMapping("/edit")
    public String updateUser(@RequestParam Long id,
                             @RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam int age,
                             @RequestParam String email,
                             @RequestParam(required = false) String password,
                             @RequestParam List<String> roles) {

        Optional<User> optionalUser = userService.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setAge(age);
            user.setEmail(email);

            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(password);
            }

            // Обновляем роли
            Set<Role> userRoles = new HashSet<>();
            for (String roleName : roles) {
                Role role = roleService.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                userRoles.add(role);
            }
            user.setRoles(userRoles);

            userService.updateUser(user);
        }

        return "redirect:/admin";
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam Long id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }

    // REST endpoint для AJAX запросов
    @GetMapping("/api/users/{id}")

    public User getUser(@PathVariable Long id) {
        return userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}