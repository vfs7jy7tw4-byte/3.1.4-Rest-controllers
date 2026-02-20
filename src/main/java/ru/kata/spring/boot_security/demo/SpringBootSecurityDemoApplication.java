package ru.kata.spring.boot_security.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;
import ru.kata.spring.boot_security.demo.repository.UserRepository;

@SpringBootApplication
public class SpringBootSecurityDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootSecurityDemoApplication.class, args);
	}

	@Bean
	CommandLineRunner init(RoleRepository roles,
						   UserRepository users,
						   PasswordEncoder encoder) {
		return args -> {

			Role roleAdmin = roles.findByName("ROLE_ADMIN")
					.orElseGet(() -> roles.save(new Role("ROLE_ADMIN")));

			Role roleUser = roles.findByName("ROLE_USER")
					.orElseGet(() -> roles.save(new Role("ROLE_USER")));

			users.findByUsername("admin").orElseGet(() -> {
				User u = new User();
				u.setUsername("admin");
				u.setPassword(encoder.encode("admin"));
				u.setEmail("admin@mail.com");
				u.getRoles().add(roleAdmin);
				u.getRoles().add(roleUser);
				return users.save(u);
			});

			users.findByUsername("user").orElseGet(() -> {
				User u = new User();
				u.setUsername("user");
				u.setPassword(encoder.encode("user"));
				u.setEmail("user@mail.com");
				u.getRoles().add(roleUser);
				return users.save(u);
			});
		};
	}
}
