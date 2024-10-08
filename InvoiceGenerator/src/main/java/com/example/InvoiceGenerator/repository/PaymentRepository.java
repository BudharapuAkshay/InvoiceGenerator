package com.example.InvoiceGenerator.repository;

import com.example.InvoiceGenerator.model.Payment;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PaymentRepository extends MongoRepository<Payment, String> {

    List<Payment> findByStatus(String status);

    @Aggregation(pipeline = { "{$group: { _id: null, total: { $sum: '$amount' } }}" })
    Double sumAllAmounts();

    Payment findByInvoicenumber(String invoiceNumber);

    List<Payment> findByPaymentdateAndStatus(String paymentDate, String status);
}
