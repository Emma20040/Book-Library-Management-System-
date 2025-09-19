package com.library.management_system.services;

import com.library.management_system.DTOs.ContactUsDto;
import com.library.management_system.models.ContactUsModel;
import com.library.management_system.models.UserModel;
import com.library.management_system.repositories.ContactUsRepository;
import com.library.management_system.repositories.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ContactService {
    private final EmailService emailService;
    private final ContactUsRepository contactUsRepository;
    private final UserRepository userRepository;


    public ContactService(EmailService emailService, ContactUsRepository contactUsRepository, UserRepository userRepository) {
        this.emailService = emailService;
        this.contactUsRepository = contactUsRepository;
        this.userRepository= userRepository;
    }

//    get current authenticated user:
    private UserModel getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            // UPDATED: Handle JWT authentication
            if (authentication.getPrincipal() instanceof Jwt) {
                try {
                    Jwt jwt = (Jwt) authentication.getPrincipal();
                    String email = jwt.getClaimAsString("email");

                    // Try to find user by email first, then by username
                    if (email != null) {
                        Optional<UserModel> userByEmail = userRepository.findByEmail(email);
                        if (userByEmail.isPresent()) {
                            return userByEmail.get();
                        }
                    }

                    return null; // User not found in database

                } catch (Exception e) {
                    System.err.println("Error extracting user from JWT: " + e.getMessage());
                    return null;
                }
            }

        }
        return null;
    }


    public ContactUsModel createContactMessage(ContactUsDto contactUsDto) {
        // Get the authenticated user
        UserModel user = getCurrentAuthenticatedUser();

        // If user is authenticated, use their info instead of the submitted form data
        String firstName = (user != null && user.getFirstName() != null) ? user.getFirstName() : contactUsDto.firstName();
        String lastName = (user != null && user.getLastName() != null) ? user.getLastName() : contactUsDto.lastName();
        String email = (user != null && user.getEmail() != null) ? user.getEmail() : contactUsDto.email();

        // Create and save the contact message
        ContactUsModel contactMessage = new ContactUsModel();
        contactMessage.setFirstName(firstName);
        contactMessage.setLastName(lastName);
        contactMessage.setEmail(email);
        contactMessage.setSubject(contactUsDto.subject());
        contactMessage.setMessage(contactUsDto.message());
        contactMessage.setUser(user);

        ContactUsModel savedMessage = contactUsRepository.save(contactMessage);

        // Send HTML notification email to admin
        try {
            String adminEmail = "emmanuelfongong10@gmail.com"; // Replace with your admin email
            String emailSubject = "New Support Message: " + contactUsDto.subject();
            String htmlContent =sendMessageToAdmin(savedMessage, user != null);

            emailService.sendHtmlEmail(adminEmail, emailSubject, htmlContent);
        } catch (MessagingException e) {
            // Log the error but don't fail the contact submission
            System.err.println("Failed to send admin notification email: " + e.getMessage());
        }

        return savedMessage;
    }

//    send message to admin
    private String sendMessageToAdmin(ContactUsModel message, boolean isAuthenticatedUser) {
        String fullName = message.getFirstName() + " " + message.getLastName();
        String userType = isAuthenticatedUser ? "Authenticated User" : "Guest User";
        String userInfo = isAuthenticatedUser ?
                "User ID: " + message.getUser().getId() :
                "No user account associated";

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px; }" +
                "        .header { background-color: #f8f9fa; padding: 15px; border-radius: 5px 5px 0 0; margin-bottom: 20px; }" +
                "        .content { margin-bottom: 20px; }" +
                "        .field { margin-bottom: 10px; }" +
                "        .field-label { font-weight: bold; color: #555; }" +
                "        .message-content { background-color: #f8f9fa; padding: 15px; border-radius: 5px; border-left: 4px solid #007bff; }" +
                "        .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #eee; font-size: 12px; color: #777; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class=\"container\">" +
                "        <div class=\"header\">" +
                "            <h2>New Support Message Received</h2>" +
                "        </div>" +
                "        <div class=\"content\">" +
                "            <div class=\"field\">" +
                "                <span class=\"field-label\">From:</span> " + fullName + " (" + message.getEmail() + ")" +
                "            </div>" +
                "            <div class=\"field\">" +
                "                <span class=\"field-label\">User Type:</span> " + userType +
                "            </div>" +
                "            <div class=\"field\">" +
                "                <span class=\"field-label\">" + (isAuthenticatedUser ? "User ID:" : "Guest Info:") + "</span> " + userInfo +
                "            </div>" +
                "            <div class=\"field\">" +
                "                <span class=\"field-label\">Subject:</span> " + message.getSubject() +
                "            </div>" +
                "            <div class=\"field\">" +
                "                <span class=\"field-label\">Received At:</span> " + message.getCreatedAt() +
                "            </div>" +
                "            <div class=\"field\">" +
                "                <span class=\"field-label\">Message:</span>" +
                "            </div>" +
                "            <div class=\"message-content\">" +
                "                " + message.getMessage().replace("\n", "<br>") +
                "            </div>" +
                "        </div>" +
                "        <div class=\"footer\">" +
                "            <p>This email was automatically generated by ReadQuest.</p>" +
                "            <p>Please do not reply to this email. To respond to the user, use their provided email address: " + message.getEmail() + "</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}

