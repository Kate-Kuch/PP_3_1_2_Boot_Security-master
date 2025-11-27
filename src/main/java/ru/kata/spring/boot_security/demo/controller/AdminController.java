package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    // Главная страница админки (возвращает данные для инициализации)
    @GetMapping("/data")
    public ResponseEntity<AdminPageData> getAdminData() {
        try {
            AdminPageData data = new AdminPageData();
            data.setUsers(userService.getAllUsers());
            data.setAllRoles(roleService.getAllRoles());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User currentUser = userService.getUserByEmail(email);
            data.setCurrentUser(currentUser);

            return new ResponseEntity<>(data, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Получить всех пользователей
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Получить пользователя по ID
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Создать пользователя
    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@RequestBody UserCreateRequest request) {
        try {
            User user = userService.createUser(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getAge(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getRoles()
            );

            UserResponse response = UserResponse.success("User created successfully", user);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            // Обработка ошибки дублирования email
            UserResponse errorResponse = UserResponse.fieldError(e.getMessage(), "email");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

        } catch (DataIntegrityViolationException e) {
            // Резервная обработка на случай, если проверка не сработала
            UserResponse errorResponse = UserResponse.fieldError("User with this email already exists", "email");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            // Общая обработка ошибок
            UserResponse errorResponse = UserResponse.error("Error creating user: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    // Обновить пользователя
    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        try {
            User user = userService.updateUser(
                    id,
                    request.getFirstName(),
                    request.getLastName(),
                    request.getAge(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getRoles()
            );

            UserResponse response = UserResponse.success("User updated successfully", user);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            // Обработка ошибки дублирования email
            UserResponse errorResponse = UserResponse.fieldError(e.getMessage(), "email");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

        } catch (RuntimeException e) {
            UserResponse errorResponse = UserResponse.error(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            UserResponse errorResponse = UserResponse.error("Error updating user: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    // Удалить пользователя
    @DeleteMapping("/users/{id}")
    public ResponseEntity<DeleteResponse> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);

            DeleteResponse response = DeleteResponse.success("User deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            DeleteResponse errorResponse = DeleteResponse.error(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            DeleteResponse errorResponse = DeleteResponse.error("Error deleting user: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    // DTO классы для запросов и ответов
    public static class UserCreateRequest {
        private String firstName;
        private String lastName;
        private int age;
        private String email;
        private String password;
        private String[] roles;

        // геттеры и сеттеры
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String[] getRoles() { return roles; }
        public void setRoles(String[] roles) { this.roles = roles; }
    }

    public static class UserUpdateRequest {
        private String firstName;
        private String lastName;
        private int age;
        private String email;
        private String password;
        private String[] roles;

        // геттеры и сеттеры
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String[] getRoles() { return roles; }
        public void setRoles(String[] roles) { this.roles = roles; }
    }

    // DTO для ответов с пользователями
    public static class UserResponse {
        private boolean success;
        private String message;
        private User user;
        private String field;

        // Приватный конструктор
        private UserResponse(boolean success, String message, User user, String field) {
            this.success = success;
            this.message = message;
            this.user = user;
            this.field = field;
        }

        // Статические фабричные методы вместо конструкторов
        public static UserResponse success(String message, User user) {
            return new UserResponse(true, message, user, null);
        }

        public static UserResponse error(String message) {
            return new UserResponse(false, message, null, null);
        }

        public static UserResponse fieldError(String message, String field) {
            return new UserResponse(false, message, null, field);
        }

        // геттеры и сеттеры
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public User getUser() { return user; }
        public void setUser(User user) { this.user = user; }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
    }

    public static class DeleteResponse {
        private boolean success;
        private String message;

        // Приватный конструктор
        private DeleteResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        // Статические фабричные методы
        public static DeleteResponse success(String message) {
            return new DeleteResponse(true, message);
        }

        public static DeleteResponse error(String message) {
            return new DeleteResponse(false, message);
        }

        // геттеры и сеттеры
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class AdminPageData {
        private List<User> users;
        private User currentUser;
        private List<Role> allRoles;

        // геттеры и сеттеры
        public List<User> getUsers() { return users; }
        public void setUsers(List<User> users) { this.users = users; }

        public User getCurrentUser() { return currentUser; }
        public void setCurrentUser(User currentUser) { this.currentUser = currentUser; }

        public List<Role> getAllRoles() { return allRoles; }
        public void setAllRoles(List<Role> allRoles) { this.allRoles = allRoles; }
    }
}