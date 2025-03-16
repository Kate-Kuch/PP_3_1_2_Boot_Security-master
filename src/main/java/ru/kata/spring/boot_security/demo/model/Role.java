package ru.kata.spring.boot_security.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Getter
@Entity
@Table(name = "roles")
public class Role implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany(mappedBy = "roles")  // mappedBy указывает, что связь управляется User
    private Set<User> users = new HashSet<>();

    public Role(String name) {
        this.name = name;
    }

    public Role() {

    }

    @Override
    public String getAuthority() {
        return name; // Spring Security использует это как роль
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id) && Objects.equals(name, role.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return name; // Теперь Thymeleaf корректно отобразит название роли
    }

}
