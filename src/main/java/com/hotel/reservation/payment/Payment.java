package com.hotel.reservation.payment;

import com.hotel.reservation.reservation.Reservation;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "payment_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(nullable = false, unique = true)
    private String transactionId;

    public Payment() {
        this.paymentDate = LocalDateTime.now();
        this.transactionId = "TXN" + System.currentTimeMillis();
    }

    // Abstract — each subclass implements its own payment logic
    public abstract boolean processPayment();

    // Concrete — shared by all subclasses
    public String generateReceipt() {
        return String.format(
                "Receipt #%s | Amount: Rs. %.2f | Date: %s | Status: %s",
                transactionId, amount, paymentDate, status
        );
    }

    @Transient
    public String getPaymentMethod() {
        return this.getClass().getSimpleName().replace("Payment", "");
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Reservation getReservation() {
        return reservation;
    }
    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }
    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}