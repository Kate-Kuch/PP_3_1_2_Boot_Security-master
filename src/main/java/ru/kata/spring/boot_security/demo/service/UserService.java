package ru.kata.spring.boot_security.demo.service;


import ru.kata.spring.boot_security.demo.model.User;
import java.util.List;

public interface UserService {

    List<User> getAllUsers();
    User getUserById(Long id);
    User getUserByEmail(String email);
    boolean existsByEmail(String email);

    // Сохранение пользователя (для обратной совместимости)
    User saveUser(User user);

    // Новые методы для бизнес-логики
    User createUser(String firstName, String lastName, int age, String email,
                    String password, String[] roles);

    User updateUser(Long userId, String firstName, String lastName, int age,
                    String email, String password, String[] roles);

    void deleteUser(Long id);
}