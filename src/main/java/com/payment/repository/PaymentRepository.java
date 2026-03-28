package com.payment.repository;

import com.payment.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByPaymentId(String paymentId);
    Optional<Payment> findFirstByPaymentIdOrderByCreatedAtDesc(String paymentId);
    List<Payment> findByStatusIn(List<Payment.PaymentStatus> statuses);
}
