package com.library.management_system.controllers;

import java.util.List;
import java.util.Map;

import com.library.management_system.DTOs.*;
import com.library.management_system.services.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

import com.library.management_system.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
public class UserController {
    private UserService userService;
    private final FileStorageService fileStorageService;

    public UserController(UserService userService, FileStorageService fileStorageService) {
        this.fileStorageService= fileStorageService;
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

//    Upload profile picture endpoint
@PostMapping(value = "/profile/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<UserProfileResponseDTO> uploadProfilePicture(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam("image") MultipartFile imageFile) {
    String email = jwt.getClaim("email");
    String imageUrl = userService.uploadProfilePicture(email, imageFile);

    ProfileUpdateRequestDTO updateRequest = new ProfileUpdateRequestDTO(
            null,
            null,
            null,
            null,
            imageUrl,
            null,
            null
    );

    UserProfileResponseDTO updatedProfile = userService.updateUserProfile(email, updateRequest);
    return ResponseEntity.ok(updatedProfile);
}

//get profile picture
    @GetMapping("/profile/picture")
    public ResponseEntity<Resource> getProfilePicture(
            @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaim("email");
        Resource imageResource = userService.getProfileImage(email);

        String filename = imageResource.getFilename();
        String contentType = fileStorageService.determineContentType(filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(imageResource);
    }


    // -------- DELETE Profile Picture --------
    @DeleteMapping("/profile/picture")
    public ResponseEntity<UserProfileResponseDTO> deleteProfilePicture(
            @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaim("email");
        UserProfileResponseDTO updatedProfile = userService.deleteProfilePicture(email);
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

//    activate user account
    @PatchMapping("/admin/users/{username}/activate")
    public ResponseEntity<Map<String, String>> activateUser(@PathVariable String username,
                                                           @AuthenticationPrincipal Jwt jwt) {
        String currentAdmin = jwt.getClaim("username");
        userService.activateUser(username, currentAdmin);
        return ResponseEntity.ok(Map.of("message", "User activated successfully"));
    }


//    get all user profiles
    @GetMapping("/admin/users/getAllUsers")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(value = "page", required = false, defaultValue =  "0") int page,
            @RequestParam(value = "size", required = false, defaultValue =  "10") int size) {

//        create pageable with sorting by creation date descending
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<UserProfileResponseDTO> users = userService.getAllUsers(pageable);

//        Extract content and create paginated response which will remove uneccesary meta data
        List<UserProfileResponseDTO> content = users.getContent();
//
        return ResponseEntity.ok(new PaginatedResponse<>(
                content,
                users.getTotalPages(),
                users.getTotalElements()));
    }



}
