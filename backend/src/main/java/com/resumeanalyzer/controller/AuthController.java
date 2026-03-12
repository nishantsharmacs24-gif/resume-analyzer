package com.resumeanalyzer.controller;

import com.resumeanalyzer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

// @RestController = This class handles HTTP requests and returns JSON
// @RequestMapping = All routes in this class start with /api/auth

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000") // Allow React to call this
public class AuthController {

    @Autowired
    private UserService userService;

    // POST /api/auth/register
    // Body: { name, email, password, role, phone }
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        Map<String, Object> result = userService.register(
            request.get("name"),
            request.get("email"),
            request.get("password"),
            request.get("role"),
            request.get("phone")
        );
        return ResponseEntity.ok(result);
    }

    // POST /api/auth/login
    // Body: { email, password }
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        Map<String, Object> result = userService.login(
            request.get("email"),
            request.get("password")
        );
        return ResponseEntity.ok(result);
    }

    // GET /api/auth/user/{id}
    // Get user profile
    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return userService.getUserById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
