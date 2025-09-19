package com.library.management_system.repositories;

import com.library.management_system.models.MailingList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface MailingRepository extends JpaRepository<MailingList, Long> {
    Optional<MailingList> findByEmail(String email);
    Optional<MailingList> findByVerificationToken(String token);
}
