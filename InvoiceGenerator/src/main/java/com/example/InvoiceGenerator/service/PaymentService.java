package com.example.InvoiceGenerator.service;

import com.example.InvoiceGenerator.dto.Paymentdto;
import com.example.InvoiceGenerator.model.Payment;
import com.example.InvoiceGenerator.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InvoiceGeneratorService invoiceGeneratorService;

    public Payment initiatePayment(Paymentdto paymentDto) {
        Payment payment = Payment.builder()
                .amount(paymentDto.getAmount())
                .currency(paymentDto.getCurrency())
                .username(paymentDto.getUsername())
                .ponumber(paymentDto.getPonumber())
                .invoicenumber(paymentDto.getInvoicenumber())
                .targetBankAccount(paymentDto.getTargetBankAccount())
                .sourceBankAccount(paymentDto.getSourceBankAccount())
                .tds(paymentDto.getTds())
                .status("PENDING")
                .paymentdate(paymentDto.getPaymentdate())
                .buyerInfo(paymentDto.getBuyerInfo())
                .receiverInfo(paymentDto.getReceiverInfo())
                .items(paymentDto.getItems().stream()
                        .map(itemdto -> new Payment.Item(itemdto.getItemName(), itemdto.getQuantity(), itemdto.getAmount()))
                        .collect(Collectors.toList()))
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Generate PDF invoice after payment is saved
        invoiceGeneratorService.generateInvoice(savedPayment);

        return savedPayment;
    }

    public List<Payment> initiatePayments(List<Paymentdto> payments) {
        List<Payment> paymentList = payments.stream().map(payment -> Payment.builder()
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .username(payment.getUsername())
                .ponumber(payment.getPonumber())
                .invoicenumber(payment.getInvoicenumber())
                .targetBankAccount(payment.getTargetBankAccount())
                .tds(payment.getTds())
                .sourceBankAccount(payment.getSourceBankAccount())
                .status(payment.getStatus())
                .paymentdate(payment.getPaymentdate())
                .build()).collect(Collectors.toList());

        return paymentRepository.saveAll(paymentList);
    }
    // 1. Find pending payments
    public List<Payment> findPendingPayments() {
        return paymentRepository.findByStatus("PENDING");
    }

    // 2. Find total amount
    public Double getTotalAmount() {
        return paymentRepository.sumAllAmounts();
    }

    // 3. Find amount by invoice number
    public Double getAmountByInvoiceNumber(String invoiceNumber) {
        Payment payment = paymentRepository.findByInvoicenumber(invoiceNumber);
        return payment != null ? payment.getAmount() : 0.0;
    }

    // 4. Find complete and pending payments by payment date
    public Map<String, List<Payment>> getPaymentsByStatusAndDate(String paymentDate) {
        Map<String, List<Payment>> paymentsByStatus = new HashMap<>();
        paymentsByStatus.put("completed", paymentRepository.findByPaymentdateAndStatus(paymentDate, "PAID"));
        paymentsByStatus.put("pending", paymentRepository.findByPaymentdateAndStatus(paymentDate, "PENDING"));
        return paymentsByStatus;
    }

    // 5. Edit payment
    public Payment editPayment(String id, Paymentdto paymentdto) {
        Optional<Payment> optionalPayment = paymentRepository.findById(id);
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            payment.setAmount(paymentdto.getAmount());
            payment.setCurrency(paymentdto.getCurrency());
            payment.setUsername(paymentdto.getUsername());
            payment.setPonumber(paymentdto.getPonumber());
            payment.setInvoicenumber(paymentdto.getInvoicenumber());
            payment.setTargetBankAccount(paymentdto.getTargetBankAccount());
            payment.setSourceBankAccount(paymentdto.getSourceBankAccount());
            payment.setTds(paymentdto.getTds());
            payment.setStatus(paymentdto.getStatus());
            payment.setPaymentdate(paymentdto.getPaymentdate());
            return paymentRepository.save(payment);
        }
        throw new RuntimeException("Payment not found");
    }

    // 6. Delete payment
    public void deletePayment(String id) {
        paymentRepository.deleteById(id);
    }
}
