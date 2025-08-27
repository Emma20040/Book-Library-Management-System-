package com.library.management_system.controllers;

import com.library.management_system.DTOs.LoginDTO;
import com.library.management_system.DTOs.UserDTO;
import com.library.management_system.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody @Valid UserDTO UserDTO) {
        userService.registerUser(UserDTO.email(), UserDTO.password(), UserDTO.username());
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

//    email verification
@GetMapping("/verify-email")
public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
    String result = userService.validateVerificationToken(token);

    if ("valid".equals(result)) {
        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    } else {
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid verification token"));
    }
}

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody @Valid LoginDTO loginDTO) {
        var token = userService.loginUser(loginDTO.emailOrUsername(), loginDTO.password());
        return ResponseEntity.ok(Map.of("token", token));
    }
}
