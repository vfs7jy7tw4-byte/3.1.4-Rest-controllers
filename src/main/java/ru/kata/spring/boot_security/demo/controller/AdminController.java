package ru.kata.spring.boot_security.demo.controller;

import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;
import ru.kata.spring.boot_security.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;

    public AdminController(UserRepository users, RoleRepository roles, PasswordEncoder encoder) {
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
    }

    @GetMapping
    public String adminPage(Model model) {
        model.addAttribute("users", users.findAll());
        model.addAttribute("newUser", new User());
        model.addAttribute("allRoles", roles.findAll());
        return "admin";
    }


    @PostMapping("/create")
    public String create(@ModelAttribute("newUser") User user,
                         @RequestParam(value = "roleIds", required = false) Set<Long> roleIds) {

        user.setPassword(encoder.encode(user.getPassword()));
        user.setRoles(loadRolesOrDefaultUserRole(roleIds));
        users.save(user);

        return "redirect:/admin";
    }


    @PostMapping("/update")
    public String update(@RequestParam("id") Long id,
                         @RequestParam("email") String email,
                         @RequestParam(value = "password", required = false) String password,
                         @RequestParam(value = "roleIds", required = false) Set<Long> roleIds) {

        User existing = users.findById(id).orElseThrow();

        existing.setEmail(email);

        if (password != null && !password.isBlank()) {
            existing.setPassword(encoder.encode(password));
        }

        existing.setRoles(loadRolesOrDefaultUserRole(roleIds));
        users.save(existing);

        return "redirect:/admin";
    }


    @PostMapping("/delete")
    public String delete(@RequestParam("id") Long id) {
        users.deleteById(id);
        return "redirect:/admin";
    }

    private Set<Role> loadRolesOrDefaultUserRole(Set<Long> roleIds) {
        Set<Role> result = new HashSet<>();

        if (roleIds != null && !roleIds.isEmpty()) {
            result.addAll(roles.findAllById(roleIds));
            return result;
        }


        Role userRole = roles.findByName("ROLE_USER").orElseThrow();
        result.add(userRole);
        return result;
    }
}
