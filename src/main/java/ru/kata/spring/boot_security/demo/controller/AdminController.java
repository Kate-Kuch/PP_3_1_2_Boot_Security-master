package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;


@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public String adminPage(Model model) {
        model.addAttribute("users", userService.getAllUsers());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userService.getUserByEmail(email);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("allRoles", roleService.getAllRoles());

        return "admin";
    }

    @GetMapping("/api/users/{id}")

    public User getUserForEdit(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // Создание пользователя
    @PostMapping("/create")
    public String createUser(@RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam int age,
                             @RequestParam String email,
                             @RequestParam String password,
                             @RequestParam(value = "roles", required = false) String[] roles) {

        userService.createUser(firstName, lastName, age, email, password, roles);
        return "redirect:/admin";
    }

    // Редактирование пользователя
    @PostMapping("/edit")
    public String editUser(@RequestParam Long userId,
                           @RequestParam String firstName,
                           @RequestParam String lastName,
                           @RequestParam int age,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam(value = "roles", required = false) String[] roles) {

        userService.updateUser(userId, firstName, lastName, age, email, password, roles);
        return "redirect:/admin";
    }

    // Удаление пользователя
    @PostMapping("/delete")
    public String deleteUser(@RequestParam Long id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }
}