package com.library.management_system.services;

import com.library.management_system.DTOs.ProfileUpdateRequestDTO;
import com.library.management_system.DTOs.UserProfileResponseDTO;
import com.library.management_system.enums.Role;
import com.library.management_system.models.UserModel;
import com.library.management_system.repositories.UserRepository;
import com.library.management_system.utils.JwtActions;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.core.io.Resource;


import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class UserService {
    @Value("${token.expiration:900}")
    private Long tokenExpirationSeconds;

    private final UserRepository userRepository;

    private final JwtActions jwtActions;

    private final EmailService emailService;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final JwtBlacklistService jwtBlacklistService;

    private final FileStorageService fileStorageService;


    public UserService(UserRepository userRepository, JwtActions jwtActions, EmailService emailService, BCryptPasswordEncoder bCryptPasswordEncoder, JwtBlacklistService jwtBlacklistService, JwtValidationService jwtValidationService, FileStorageService fileStorageService) {
    
        this.userRepository = userRepository;
        this.jwtActions = jwtActions;
        this.emailService = emailService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtBlacklistService = jwtBlacklistService;
        this.fileStorageService = fileStorageService;

    }

    private Optional<UserModel> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    private Optional<UserModel> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    private Optional<UserModel> findUserByEmailOrUsername(String emailOrUsername) {
        Optional<UserModel> userByEmail = findUserByEmail((emailOrUsername));
        if (userByEmail.isPresent()) {
            return userByEmail;
        }
        return findUserByUsername(emailOrUsername);
    }

    private boolean verifyPassword(String rawPassword, String encodedPassword) {
        return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
    }

    //    Register new user
    public void registerUser(String email, String password, String username) {
        if (findUserByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        var encodedPassword = bCryptPasswordEncoder.encode(password);

        var newUser = new UserModel(null, username, email, encodedPassword, false, null, Role.USER);


//        // Generate verification token and send email
        String token = UUID.randomUUID().toString();
        newUser.setVerificationToken(token);

        String baseUrl ="http://localhost:8080";
//        String baseUrl ="https://e218876891ff.ngrok-free.app";
        String link = baseUrl + "/user/verify-email?token=" + token;
        newUser.isEnabled(); //automatically set to false untill email is validated
        try {
            emailService.sendEmail(email, "Verify your email", "Click the link below to verify your email: " + link);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        // Save user to database
        userRepository.save(newUser);
    }

//    validating verification token
public String validateVerificationToken(String token) {
    UserModel user = userRepository.findByVerificationToken(token).orElse(null);
    if (user == null) {
        return "invalid";
    }

    user.setEnabled(true);
    user.setVerificationToken(null); // Clear the token after verification
    userRepository.save(user);
    return "valid";
}

//login user and generate token
    public String loginUser(String emailOrUsername, String password) {
        var user = findUserByEmailOrUsername(emailOrUsername).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid login credentials"));

        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email not verified verify email and try again");
        }

        if (!verifyPassword(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid login credentials");
        }
        return jwtActions.jwtCreate(user.getId(),user.getEmail(), user.getUsername(), user.getRole().toString());
    }


//    Logs out a user by blacklisting their jwt token
    public void logoutUser(String token) {
        try{
//            // Decode the token to get its expiration time
            var jwt = jwtActions.decodeToken(token);
            Instant expiration= jwt.getExpiresAt();

            // Add token to blacklist with its natural expiration time
            jwtBlacklistService.blacklistToken(token, expiration);

        }  catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token");
        }
    }


//    -------- PASSWORD RESET --------
//    send pasword reset email
    public void sendPasswordResetEmail(String email, String token){
        String baseUrl ="http://localhost:8080";
        String link = baseUrl + "/user/reset?token=" + token;

        try {
            emailService.sendEmail(email, "Password Reset Request", "Click the link to reset your password:  " + link);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

//     genearte token for the user that wants to chande password and send email
    public void redeemPassword(String email){
        var user = findUserByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid email"));
        var token = UUID.randomUUID().toString();
        user.withResetToken(token, Instant.now().plusSeconds(this.tokenExpirationSeconds));

        userRepository.save(user);
        sendPasswordResetEmail(user.getEmail(), token);
    }

//    create new password
    public void resetPassword(String token, String password){
        Instant now = Instant.now();
        var user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "user not found"));
        if (user.getResetTokenExpiration().isBefore(now)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "token expired");
        }

        user.setPassword(bCryptPasswordEncoder.encode(password));

        user.withResetToken(null, null);
        userRepository.save(user);
    }


//    -------USER PROFILE-------

//    get user profile by email
    public  UserProfileResponseDTO getUserProfile(String email){
        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "user not found"));
        return mapToProfileResponseDTO(user);
    }

//only updates provided fields for user progile : patch
public UserProfileResponseDTO updateUserProfile(String email, ProfileUpdateRequestDTO updateRequest) {
    UserModel user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    // Update only the profile fields that are provided (not null)
    // Users can provide only the fields they want to update
    if (updateRequest.firstName() != null) {
        user.setFirstName(updateRequest.firstName());
    }
    if (updateRequest.lastName() != null) {
        user.setLastName(updateRequest.lastName());
    }
    if (updateRequest.address() != null) {
        user.setAddress(updateRequest.address());
    }
    if (updateRequest.country() != null) {
        user.setCountry(updateRequest.country());
    }
    if (updateRequest.profileImageUrl() != null) {
        user.setProfileImageUrl(updateRequest.profileImageUrl());
    }
    if (updateRequest.bio() != null) {
        user.setBio(updateRequest.bio());
    }
    if (updateRequest.phoneNumber() != null) {
        user.setPhoneNumber(updateRequest.phoneNumber());
    }

    UserModel updatedUser = userRepository.save(user);
    return mapToProfileResponseDTO(updatedUser);
}

//-------- HANDLING user profile image ----------

//    Upload profile picture - returns the image URL only
    public String uploadProfilePicture(String email, MultipartFile profileImage) {
        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        try {
            // Generate safe filename
            String filename = generateProfileImageFilename(email, profileImage.getOriginalFilename());

            // Use FileStorageService to store the image
            String filePath = fileStorageService.storeProfilePicture(profileImage, filename);

            // Delete old profile image if exists
            if (user.getProfileImageUrl() != null) {
                fileStorageService.deleteFile(user.getProfileImageUrl());
            }

            // Return the file path (URL) - caller can use this in updateUserProfile
            return filePath;

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload profile image: " + e.getMessage());
        }
    }


//    Get profile image
    public Resource getProfileImage(String email) {
        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getProfileImageUrl() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User has no profile image");
        }

        // Extract filename from stored path and load using FileStorageService
        String filename = fileStorageService.extractFilenameFromPath(user.getProfileImageUrl());
        return fileStorageService.loadProfilePicture(filename);
    }


    // Delete profile picture - returns updated user profile as Response DTO
    public UserProfileResponseDTO deleteProfilePicture(String email) {
        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getProfileImageUrl() != null) {
            fileStorageService.deleteFile(user.getProfileImageUrl());
            user.setProfileImageUrl(null);
            UserModel updatedUser = userRepository.save(user);
            return mapToProfileResponseDTO(updatedUser); // Return Response DTO
        }

        return mapToProfileResponseDTO(user); // Return Response DTO
    }


    // Helper method to generate safe filename for profile image
    private String generateProfileImageFilename(String email, String originalFilename) {
        String cleanEmail = email.replaceAll("[^a-zA-Z0-9]", "-").toLowerCase();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return cleanEmail + "-profile-" + System.currentTimeMillis() + extension;
    }




//    helper method for user profile
private UserProfileResponseDTO mapToProfileResponseDTO(UserModel user) {
    return new UserProfileResponseDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.getFirstName(),
            user.getLastName(),
            user.getCountry(),
            user.getAddress(),
            user.getProfileImageUrl(),
            user.getBio(),
            user.getPhoneNumber()
    );
}


//--------- ADMIN FUNCTIONS RELATED TO USERS -----------

//count the total number of users
    public long countUsers() {

        return userRepository.count();
    }


    //method for admin to get user info by username using user profile
    public UserProfileResponseDTO getUserProfileByUsername(String username){
        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "user not found"));
        return mapToProfileResponseDTO(user);
    }


//    get the profile information for all user information for only admins
    public Page<UserProfileResponseDTO> getAllUsers(Pageable pageable) {
        // sort ordering by creation date
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("createdAt").descending()
        );

        Page<UserModel> users= userRepository.findAll(sortedPageable);
        return users.map(this::mapToProfileResponseDTO);
    }


// Delete user
    public void deleteUser(String username, String currentAdmin){
        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "user not found "));

        if (user.getRole() == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "can not delete another admin");

        }

        if( username.equals(currentAdmin)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "can not delete your account");
        }

        userRepository.delete(user);

    }


//    suspend user
    public void suspendUser(String username, String currentAdmin){
        UserModel  user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "can't find user"));

        if (user.getRole() == Role.ADMIN){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "can't suspend another admin");
        }

        if (username.equals(currentAdmin)){
            throw  new ResponseStatusException(HttpStatus.FORBIDDEN, "can not suspen your own");
        }

        user.setEnabled(false);
        userRepository.save(user);
    }

//    activate suspended user account
    public void activateUser(String username, String currentAdmin){
        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "can't find user"));

        if (user.getRole() == Role.ADMIN){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "can't activate another admin");
        }

        if (username.equals(currentAdmin)){
            throw  new ResponseStatusException(HttpStatus.FORBIDDEN, "can not activate your own");
        }

        if (user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "user is already active");
        }

        user.setEnabled(true);
        userRepository.save(user);

    }



}



