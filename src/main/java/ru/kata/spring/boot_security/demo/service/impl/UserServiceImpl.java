package ru.kata.spring.boot_security.demo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.UserRepository;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    // Важно: добавить self-инжектирование для обхода ограничения Spring AOP
    private UserService self;

    // Объект для синхронизации по email (для предотвращения race condition)
    private final Map<String, Object> emailLocks = new ConcurrentHashMap<>();

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleService roleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User saveUser(User user) {
        // Для обратной совместимости - проверяем дублирование email
        if (user.getId() == null) {
            // Новый пользователь - проверяем email
            // Используем self вместо this для вызова через прокси
            if (self.existsByEmail(user.getEmail())) {
                throw new IllegalArgumentException("Пользователь с email " + user.getEmail() + " уже существует");
            }
        } else {
            // Существующий пользователь - проверяем, не занят ли email другим пользователем
            User existingUser = self.getUserById(user.getId());
            if (!existingUser.getEmail().equals(user.getEmail()) && self.existsByEmail(user.getEmail())) {
                throw new IllegalArgumentException("Пользователь с email " + user.getEmail() + " уже существует");
            }
        }

        // Шифруем пароль если он не зашифрован
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(user);
    }

    @Override
    public User createUser(String firstName, String lastName, int age, String email,
                           String password, String[] roles) {

        // Синхронизируем по email чтобы избежать race condition
        Object lock = emailLocks.computeIfAbsent(email.toLowerCase(), k -> new Object());

        synchronized (lock) {
            try {
                // ПРОВЕРКА НА СУЩЕСТВОВАНИЕ ПОЛЬЗОВАТЕЛЯ С ТАКИМ EMAIL
                // Используем self вместо this
                if (self.existsByEmail(email)) {
                    throw new IllegalArgumentException("Пользователь с email " + email + " уже существует");
                }

                User user = new User();
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setAge(age);
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode(password));

                setUserRoles(user, roles);

                return userRepository.save(user);
            } finally {
                // Очищаем lock чтобы не накапливать в памяти
                emailLocks.remove(email.toLowerCase());
            }
        }
    }

    @Override
    public User updateUser(Long userId, String firstName, String lastName, int age,
                           String email, String password, String[] roles) {
        // Используем self вместо this
        User existingUser = self.getUserById(userId);

        // Если email меняется, проверяем новый email с синхронизацией
        if (!existingUser.getEmail().equals(email)) {
            Object lock = emailLocks.computeIfAbsent(email.toLowerCase(), k -> new Object());

            synchronized (lock) {
                try {
                    // Используем self вместо this
                    if (self.existsByEmail(email)) {
                        throw new IllegalArgumentException("Пользователь с email " + email + " уже существует");
                    }
                } finally {
                    emailLocks.remove(email.toLowerCase());
                }
            }
        }

        existingUser.setFirstName(firstName);
        existingUser.setLastName(lastName);
        existingUser.setAge(age);
        existingUser.setEmail(email);

        // Обновляем пароль только если он не пустой
        if (password != null && !password.trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(password));
        }

        setUserRoles(existingUser, roles);

        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) {
        // Используем self вместо this
        User user = self.getUserById(id);
        userRepository.delete(user);
    }

    // Вспомогательный метод для установки ролей пользователя
    private void setUserRoles(User user, String[] roles) {
        if (roles != null && roles.length > 0) {
            Set<Role> userRoles = Arrays.stream(roles)
                    .map(roleService::getRoleByName)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            user.setRoles(userRoles);
        } else {
            // Устанавливаем роль USER по умолчанию, если роли не указаны
            roleService.getRoleByName("ROLE_USER")
                    .ifPresent(role -> user.setRoles(Set.of(role)));
        }
    }
}