package com.library.management_system.services;

import com.library.management_system.DTOs.MailingListDto;
import com.library.management_system.models.MailingList;
import com.library.management_system.models.UserModel;
import com.library.management_system.repositories.MailingRepository;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
public class MailingService {
    private final EmailService emailService;
    private final MailingRepository mailingRepository;


    public MailingService(EmailService emailService, MailingRepository mailingRepository) {
        this.emailService = emailService;
        this.mailingRepository = mailingRepository;
    }

    //    check if email already exist
    private Optional<MailingList> findByEmail(String email) {
        return mailingRepository.findByEmail(email);
    }

    public void subscribe(String email) {
        if (findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "you have already subscribe ");
        }


        String token = UUID.randomUUID().toString();
        var subscriber = new MailingList(null, email, token,null);
        subscriber.setVerificationToken(token);

        mailingRepository.save(subscriber);

        String baseUrl = "http://localhost:8080";
        String verificationLink = baseUrl + "/api/mailing/verify-email?token=" + token;

        try {
            emailService.sendEmail(email, "click on the link to confirm you subscribtion to our mailing list", verificationLink);
        } catch (MessagingException e) {

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to send verification email. Please try again.");
        }


    }

    public String validateVerificationToken(String token) {
        Optional<MailingList> subscriberOpt = mailingRepository.findByVerificationToken(token);

        if (subscriberOpt.isEmpty()) {
            return "invalid";
        }

        MailingList subscriber = subscriberOpt.get();
        subscriber.setVerificationToken(null); // Clear the token after verification
        mailingRepository.save(subscriber);
        return "valid";
    }
}
