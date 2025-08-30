package com.library.management_system.models;
import com.library.management_system.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import  java.util.UUID;

@Entity
@Table(name="users")
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private  UUID id;

    @Column(unique = true, nullable = false, updatable = false)
    private String username;

    @Column(unique = true, nullable = false, updatable = true)
    private String email;

    @Column(nullable = false, updatable = true)
    private String password;

    @Column(nullable = false, updatable = true)
    private boolean enabled = false;

    @Column()
    private String verificationToken;

    @Enumerated(EnumType.STRING)
    private Role role;

//    password reset
    @Column()
    private String resetToken;

    @Column
    private Instant resetTokenExpiration;


//    attributes for user profile
    @Column()
    private String firstName;

    @Column()
    private String lastName;

    @Column()
    private String address;

    @Column()
    private String bio;

    @Column()
    private String phoneNumber;

    @Column()
    private String country;

    @Column()
    private String profileImageUrl;



//empty user constructor
    public UserModel(){

    }

    // Constructor for mandatory fields during registration

    public UserModel(UUID id, String username, String email, String password, boolean enabled, String verificationToken, Role role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.enabled = false;
        this.verificationToken = null;
        this.role = role;
    }

//    constructor for reset password
public UserModel withResetToken(String resetToken, Instant resetTokenAdditionalTime) {
    this.resetToken = resetToken;
    this.resetTokenExpiration = resetTokenAdditionalTime;
    return this;
}

    //       getters and setters
    public UUID getId() {

        return id;
    }

    public void setId(UUID id) {

        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {

        this.email = email;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }


    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {

        this.role = role;
    }


    public boolean isEnabled() {

        return enabled;
    }

    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
    }

    public String getVerificationToken() {

        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {

        this.verificationToken = verificationToken;
    }

    public String getResetToken() {

        return resetToken;
    }

    public void setResetToken(String resetToken) {

        this.resetToken = resetToken;
    }

    public Instant getResetTokenExpiration() {

        return resetTokenExpiration;
    }

    public void setResetTokenExpiration(Instant resetTokenExpiration) {
        this.resetTokenExpiration = resetTokenExpiration;
    }

//    getters and setters for user profile

    public String getFirstName() {

        return firstName;
    }

    public void setFirstName(String firstName) {

        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {

        this.lastName = lastName;
    }

    public String getAddress() {

        return address;
    }

    public void setAddress(String address) {

        this.address = address;
    }

    public String getBio() {

        return bio;
    }

    public void setBio(String bio) {

        this.bio = bio;
    }

    public String getPhoneNumber() {

        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {

        this.phoneNumber = phoneNumber;
    }

    public String getCountry() {

        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
