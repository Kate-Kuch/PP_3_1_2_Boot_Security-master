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
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

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
    public User createUser(String firstName, String lastName, int age, String email,
                           String password, String[] roles) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAge(age);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        setUserRoles(user, roles);

        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long userId, String firstName, String lastName, int age,
                           String email, String password, String[] roles) {
        User existingUser = getUserById(userId);

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
        User user = getUserById(id);
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public User saveUser(User user) {
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    //Вспомогательный метод для установки ролей пользователя
    private void setUserRoles(User user, String[] roles) {
        if (roles != null && roles.length > 0) {
            Set<Role> userRoles = Arrays.stream(roles)
                    .map(roleService::getRoleByName)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            user.setRoles(userRoles);
        } else {
            user.setRoles(new HashSet<>());
        }
    }
}