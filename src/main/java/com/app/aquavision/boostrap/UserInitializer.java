package com.app.aquavision.boostrap;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.app.aquavision.entities.Role;
import com.app.aquavision.entities.User;
import com.app.aquavision.repositories.RoleRepository;
import com.app.aquavision.repositories.UserRepository;

import jakarta.transaction.Transactional;

@Component
@Order(3)
public class UserInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        createUserIfNotExists("testuser", "test123", List.of("ROLE_USER"));
        createUserIfNotExists("testuseradmin", "admin123", List.of("ROLE_USER", "ROLE_ADMIN"));
    }

    private void createUserIfNotExists(String username, String password, List<String> roleNames) {
        if (userRepository.findByUsername(username).isPresent()) {
            System.out.println("User already exists: " + username);
            return;
        }

        List<Role> roles = new ArrayList<>();
        for (String roleName : roleNames) {
            Optional<Role> optionalRole = roleRepository.findByName(roleName);
            if (optionalRole.isEmpty()) {
                System.out.println("Role not found: " + roleName + ". Skipping user creation.");
                return;
            }
            roles.add(optionalRole.get());
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        user.setRoles(roles);

        userRepository.save(user);
        System.out.println("User created: " + username);
    }
}
