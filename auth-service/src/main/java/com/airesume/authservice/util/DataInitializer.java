package com.airesume.authservice.util;

import com.airesume.authservice.model.PlanType;
import com.airesume.authservice.model.ProviderType;
import com.airesume.authservice.model.Role;
import com.airesume.authservice.model.User;
import com.airesume.authservice.repository.RoleRepository;
import com.airesume.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Ensures that basic data (Roles, Admin User) exists on startup.
 * Prevents loss of Admin access after a database refresh.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default-admin.username}")
    private String adminUsername;
    
    @Value("${app.default-admin.password}")
    private String adminPassword;
    
    @Value("${app.default-admin.email}")
    private String adminEmail;

    @Override
    public void run(String... args) {
        log.info("Checking for default roles and admin user...");
        
        // 1. Ensure ROLE_ADMIN exists
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> {
                    log.info("Creating ROLE_ADMIN...");
                    return roleRepository.save(new Role(null, "ROLE_ADMIN"));
                });

        // 2. Ensure ROLE_USER exists
        roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    log.info("Creating ROLE_USER...");
                    return roleRepository.save(new Role(null, "ROLE_USER"));
                });

        // 3. Ensure Default Admin User exists
        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            log.info("Creating default admin user...");
            
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);

            User admin = User.builder()
                    .fullName("System Administrator")
                    .username(adminUsername)
                    .email(adminEmail)
                    .mobileNumber("9999999999")
                    .password(passwordEncoder.encode(adminPassword))
                    .subscriptionPlan(PlanType.PREMIUM)
                    .provider(ProviderType.LOCAL)
                    .isActive(true)
                    .enabled(true)
                    .roles(roles)
                    .build();

            userRepository.save(admin);
            log.info("Default admin user created successfully (username: {})", adminUsername);
        } else {
            log.info("Admin user already exists.");
        }
    }
}
