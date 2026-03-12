package com.resumeanalyzer.service;

import com.resumeanalyzer.model.User;
import com.resumeanalyzer.repository.UserRepository;
import com.resumeanalyzer.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// @Service = This is a business logic layer class
// Controllers call Services, Services call Repositories

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // BCrypt encoder from SecurityConfig

    @Autowired
    private JwtUtil jwtUtil;

    // REGISTER a new user
    public Map<String, Object> register(String name, String email,
                                         String password, String role, String phone) {
        Map<String, Object> response = new HashMap<>();

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            response.put("success", false);
            response.put("message", "Email already registered!");
            return response;
        }

        // Create new user
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password)); // Hash the password!
        user.setRole(User.Role.valueOf(role.toUpperCase())); // Convert string to enum
        user.setPhone(phone);

        // Save to database
        User savedUser = userRepository.save(user);

        // Generate JWT token for immediate login after registration
        String token = jwtUtil.generateToken(email, role.toUpperCase(), savedUser.getId());

        response.put("success", true);
        response.put("message", "Registration successful!");
        response.put("token", token);
        response.put("userId", savedUser.getId());
        response.put("name", savedUser.getName());
        response.put("role", savedUser.getRole().toString());

        return response;
    }

    // LOGIN an existing user
    public Map<String, Object> login(String email, String password) {
        Map<String, Object> response = new HashMap<>();

        // Find user by email
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            response.put("success", false);
            response.put("message", "No account found with this email!");
            return response;
        }

        User user = optionalUser.get();

        // Check if password matches (BCrypt compare)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.put("success", false);
            response.put("message", "Incorrect password!");
            return response;
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(email, user.getRole().toString(), user.getId());

        response.put("success", true);
        response.put("message", "Login successful!");
        response.put("token", token);
        response.put("userId", user.getId());
        response.put("name", user.getName());
        response.put("role", user.getRole().toString());

        return response;
    }

    // GET user profile by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
}
