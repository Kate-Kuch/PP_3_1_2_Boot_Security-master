package ru.kata.spring.boot_security.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "ru.kata.spring.boot_security.demo.repository") // Репозитории
@EntityScan(basePackages = "ru.kata.spring.boot_security.demo.model")
public class SpringBootSecurityDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootSecurityDemoApplication.class, args);
	}

}
