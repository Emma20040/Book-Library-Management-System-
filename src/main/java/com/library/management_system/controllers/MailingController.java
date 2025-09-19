package com.library.management_system.controllers;

import com.library.management_system.DTOs.MailingListDto;
import com.library.management_system.services.MailingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mailing")
public class MailingController {
    private final MailingService mailingService;

    public MailingController(MailingService mailingService) {
        this.mailingService = mailingService;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, String>> subscribeToMail(@RequestBody @Valid MailingListDto mailingListDto){
        mailingService.subscribe(mailingListDto.email());
        return ResponseEntity.ok(Map.of("message", "thanks for subscribing to our mailing list, check your email and confirm"));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token){
        String results = mailingService.validateVerificationToken(token);
        if("valid".equals(results)){
            return ResponseEntity.ok(Map.of("message", "Email verified successfully! You are now subscribed"));
        }
        else{
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid verification token"));
        }
    }
}
