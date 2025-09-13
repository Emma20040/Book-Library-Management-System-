package com.library.management_system.repositories;


import com.library.management_system.models.Book;
import com.library.management_system.models.BookAccess;
import com.library.management_system.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BookAccessRepository extends JpaRepository<BookAccess, Long> {
    @Query("SELECT ba FROM BookAccess ba WHERE ba.user = :user AND ba.book = :book AND :currentDate BETWEEN ba.startDate AND ba.endDate")
    Optional<BookAccess> findActiveAccess(UserModel user, Book book, LocalDateTime currentDate);
}
