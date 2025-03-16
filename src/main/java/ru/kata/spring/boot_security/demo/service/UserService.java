package ru.kata.spring.boot_security.demo.service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;
import ru.kata.spring.boot_security.demo.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Регистрация нового пользователя
    public void registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    // Найти всех пользователей
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // Найти пользователя по ID
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // Найти пользователя по имени (логину)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Удалить пользователя по ID
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}





