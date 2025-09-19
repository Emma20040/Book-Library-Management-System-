package com.library.management_system.controllers;

import com.library.management_system.DTOs.ContactUsDto;
import com.library.management_system.models.ContactUsModel;
import com.library.management_system.services.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/contact")
public class ContactController {
    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<ContactUsModel> sendMessage(@Valid @RequestBody ContactUsDto contactUsDto) {
        ContactUsModel message = contactService.createContactMessage(contactUsDto);
        return ResponseEntity.ok(message);
    }
}
