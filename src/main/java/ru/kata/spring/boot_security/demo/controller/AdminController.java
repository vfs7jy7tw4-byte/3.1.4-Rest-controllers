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
    public String usersTable(Model model) {
        model.addAttribute("users", users.findAll());
        return "admin";
    }

    @GetMapping("/new")
    public String newUser(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roles.findAll());
        return "new-user";
    }


    @PostMapping("/new")
    public String createUser(@ModelAttribute("user") User user,
                             @RequestParam(value = "roleIds", required = false) Set<Long> roleIds) {

        user.setPassword(encoder.encode(user.getPassword()));
        user.setRoles(loadRolesOrDefaultUserRole(roleIds));
        users.save(user);

        return "redirect:/admin";
    }


    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        User existing = users.findById(id).orElseThrow();
        model.addAttribute("user", existing);
        model.addAttribute("allRoles", roles.findAll());
        return "edit-user";
    }


    @PostMapping("/edit")
    public String updateUser(@RequestParam("id") Long id,
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


    @GetMapping("/delete/{id}")
    public String deleteConfirm(@PathVariable Long id, Model model) {
        model.addAttribute("user", users.findById(id).orElseThrow());
        return "delete-user";
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam("id") Long id) {
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