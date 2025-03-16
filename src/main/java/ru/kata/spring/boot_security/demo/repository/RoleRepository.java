package ru.kata.spring.boot_security.demo.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.kata.spring.boot_security.demo.model.Role;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    List<Role> findAllByName(String name);
    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE :role MEMBER OF u.roles")
    void detachUsersFromRole(@Param("role") Role role);
}

