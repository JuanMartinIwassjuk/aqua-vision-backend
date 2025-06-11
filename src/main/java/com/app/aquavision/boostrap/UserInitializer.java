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
@Order(2)
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
        createTestUserIfNotExists();
    }

    private void createTestUserIfNotExists() {
        String username = "testuser";
        String password = "test123";

        if (userRepository.findByUsername(username).isPresent()) {
            System.out.println("Test user already exists: " + username);
            return;
        }

        Optional<Role> optionalRole = roleRepository.findByName("ROLE_USER");
        if (optionalRole.isEmpty()) {
            System.out.println("ROLE_USER not found. Skipping user creation.");
            return;
        }

        Role roleUser = optionalRole.get();

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        
        List<Role> roles = new ArrayList<>();
        roles.add(roleUser);
        user.setRoles(roles);

        userRepository.save(user);
        System.out.println("Test user created: " + username);
    }
}