package com.library.management_system.repositories;


import com.library.management_system.models.Transaction;
import com.library.management_system.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    @Query("SELECT t FROM Transaction t JOIN FETCH t.user WHERE t.stripePaymentIntentId = :stripePaymentIntentId")
    Optional<Transaction> findByStripePaymentIntentId(String stripePaymentIntentId);
    List<Transaction> findByUser(UserModel user);


    long count();

    @Query("SELECT COUNT(t) FROM Transaction t  WHERE t.user = :user")
    long countAllTransations(@Param("user") UserModel user);

}
