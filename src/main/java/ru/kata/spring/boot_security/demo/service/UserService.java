package ru.kata.spring.boot_security.demo.service;


import ru.kata.spring.boot_security.demo.model.User;
import java.util.List;

public interface UserService {

    List<User> getAllUsers();

    User getUserById(Long id);

    User getUserByEmail(String email);

    User saveUser(User user);

    User updateUser(User updatedUser); // Принимает готового пользователя

    void deleteUser(Long id);

    boolean existsByEmail(String email);
}