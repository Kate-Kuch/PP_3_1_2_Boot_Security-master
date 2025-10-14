package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


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
    @ResponseBody
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

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAge(age);
        user.setEmail(email);
        user.setPassword(password);

        if (roles != null && roles.length > 0) {
            Set<Role> userRoles = Arrays.stream(roles)
                    .map(roleName -> roleService.getRoleByName(roleName))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            user.setRoles(userRoles);
        }

        userService.saveUser(user);
        return "redirect:/admin";
    }

    // Редактирование пользователя - БЕЗ setId
    @PostMapping("/edit")
    public String editUser(@RequestParam Long userId,
                           @RequestParam String firstName,
                           @RequestParam String lastName,
                           @RequestParam int age,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam(value = "roles", required = false) String[] roles) {

        // Получаем существующего пользователя по ID
        User existingUser = userService.getUserById(userId);

        // Обновляем поля
        existingUser.setFirstName(firstName);
        existingUser.setLastName(lastName);
        existingUser.setAge(age);
        existingUser.setEmail(email);

        // Обновляем пароль только если он не пустой
        if (password != null && !password.trim().isEmpty()) {
            existingUser.setPassword(password);
        }

        // Обновляем роли
        if (roles != null && roles.length > 0) {
            Set<Role> userRoles = Arrays.stream(roles)
                    .map(roleName -> roleService.getRoleByName(roleName))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            existingUser.setRoles(userRoles);
        }

        userService.updateUser(existingUser);
        return "redirect:/admin";
    }

    // Удаление пользователя
    @PostMapping("/delete")
    public String deleteUser(@RequestParam Long id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }
}