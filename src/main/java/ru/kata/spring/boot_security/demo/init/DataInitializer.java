package ru.kata.spring.boot_security.demo.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;
import ru.kata.spring.boot_security.demo.repository.UserRepository;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Создаем роли, если они еще не существуют
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(new Role("ROLE_ADMIN"));
            roleRepository.save(new Role("ROLE_USER"));
        }

        // Создаем администратора, если он еще не существует
        if (userRepository.findByEmail("admin@mail.ru").isEmpty()) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
            Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow();

            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("AdminLastName");
            admin.setAge(45);
            admin.setUsername("admin");
            admin.setEmail("admin@mail.ru"); // Устанавливаем email
            admin.setPassword(passwordEncoder.encode("admin123")); // Шифруем пароль
            admin.setRoles(Set.of(adminRole, userRole)); // Назначаем роли
            userRepository.save(admin);
        }

        // Создаем обычного пользователя, если он еще не существует
        if (userRepository.findByEmail("user@mail.ru").isEmpty()) {
            Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow();

            User user = new User();
            user.setFirstName("User");
            user.setLastName("UserLastName");
            user.setAge(30);
            user.setUsername("user");
            user.setEmail("user@mail.ru"); // Устанавливаем email
            user.setPassword(passwordEncoder.encode("user123")); // Шифруем пароль
            user.setRoles(Set.of(userRole)); // Назначаем роль
            userRepository.save(user);
        }
    }
}