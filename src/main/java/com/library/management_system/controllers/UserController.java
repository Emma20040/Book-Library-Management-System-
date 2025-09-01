package com.library.management_system.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library.management_system.DTOs.LoginDTO;
import com.library.management_system.DTOs.ProfileUpdateRequestDTO;
import com.library.management_system.DTOs.UserDTO;
import com.library.management_system.DTOs.UserProfileResponseDTO;
import com.library.management_system.DTOs.UserRedeemPasswordDto;
import com.library.management_system.DTOs.UserResetPasswordDto;
import com.library.management_system.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {

        this.userService = userService;
    }

    //    --------- SIGNUP USER --------
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


    //    ----------- LOGIN  ---------
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody @Valid LoginDTO loginDTO) {
        var token = userService.loginUser(loginDTO.emailOrUsername(), loginDTO.password());
        return ResponseEntity.ok(Map.of("token", token));
    }


    //    ---------- LOGOUT --------
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logoutUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            userService.logoutUser(token);
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid authorization header"));
    }


    //    -------- PASSWORD RESET --------
    //    send email link toress password
    @PostMapping("/redeem-password")
    public ResponseEntity<Map<String, String>> redeemPassword(@RequestBody @Valid UserRedeemPasswordDto userRedeemPasswordDto) {
        userService.redeemPassword(userRedeemPasswordDto.email());
        return ResponseEntity.ok().body(Map.of("message", "Send the redeem password link to your email"));
    }


    //    route to reset password
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody @Valid UserResetPasswordDto userResetPasswordDto) {
        userService.resetPassword(userResetPasswordDto.token(), userResetPasswordDto.password());
        return ResponseEntity.ok().body(Map.of("message", "Credentials updated successfully"));
    }

    //-------- USER PROFILE ---------
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponseDTO> getCurrentUserProfile(
            @AuthenticationPrincipal Jwt jwt) {
        // just gebuging to  see all claims and other info
        System.out.println("JWT Claims:///////////////////////// " + jwt.getClaims() + " " + " " + jwt.getSubject() + " " + " " + jwt.getHeaders());
        String email = jwt.getClaim("email");
        UserProfileResponseDTO profile = userService.getUserProfile(email);
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/profile")
    public ResponseEntity<UserProfileResponseDTO> updateUserProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ProfileUpdateRequestDTO updateProfileRequest) {
        String email = jwt.getClaim("email");
        UserProfileResponseDTO updatedProfile = userService.updateUserProfile(email, updateProfileRequest);
        return ResponseEntity.ok(updatedProfile);
    }

    //    admin route to get user profile infor via username
    @GetMapping("/admin/users/{username}")
    public ResponseEntity<UserProfileResponseDTO> getUserProfileByUsername(@PathVariable String username) {
        UserProfileResponseDTO profile = userService.getUserProfileByUsername(username);
        return ResponseEntity.ok(profile);
    }

    // Get total number of users for admins only
    @GetMapping("/countUsers")
    public ResponseEntity<Long> getTotalUsers() {
        Long totalUsers = userService.countUsers();
        return ResponseEntity.ok(totalUsers);
    }

    //    delete user
    @DeleteMapping("/admin/users/{username}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String username,
                                                          @AuthenticationPrincipal Jwt jwt) {
        String currentAdmin = jwt.getClaim("username");
        userService.deleteUser(username, currentAdmin);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));

    }

//suspend user account
    @PatchMapping("/admin/users/{username}/suspend")
    public ResponseEntity<Map<String, String>> suspendUser(@PathVariable String username,
                                                           @AuthenticationPrincipal Jwt jwt) {
        String currentAdmin = jwt.getClaim("username");
        userService.suspendUser(username, currentAdmin);
        return ResponseEntity.ok(Map.of("message", "User suspended successfully"));
    }

//    activate user
    @PatchMapping("/admin/users/{username}/activate")
    public ResponseEntity<Map<String, String>> activateUser(@PathVariable String username,
                                                           @AuthenticationPrincipal Jwt jwt) {
        String currentAdmin = jwt.getClaim("username");
        userService.activateUser(username, currentAdmin);
        return ResponseEntity.ok(Map.of("message", "User activated successfully"));
    }



}
