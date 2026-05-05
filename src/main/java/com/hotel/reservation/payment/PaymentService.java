package com.hotel.reservation.payment;

import com.hotel.reservation.reservation.Reservation;
import com.hotel.reservation.reservation.ReservationService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository repository;
    private final ReservationService reservationService;

    public PaymentService(PaymentRepository repository, ReservationService reservationService) {
        this.repository = repository;
        this.reservationService = reservationService;
    }

    public List<Payment> getAll() {
        return repository.findAll();
    }

    public Payment getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + id));
    }

    public Payment getByReservation(Reservation reservation) {
        return repository.findByReservation(reservation).orElse(null);
    }

    public boolean isPaid(Reservation reservation) {
        return repository.existsByReservation(reservation);
    }

    public Payment processCashPayment(Long reservationId, String receivedBy) {
        Reservation reservation = reservationService.getById(reservationId);
        validatePayable(reservation);

        CashPayment payment = new CashPayment();
        payment.setReservation(reservation);
        payment.setAmount(reservation.getTotalAmount());
        payment.setReceivedBy(receivedBy);

        boolean ok = payment.processPayment();  // <-- polymorphic call
        if (!ok) {
            throw new IllegalArgumentException("Cash payment failed.");
        }

        Payment saved = repository.save(payment);
        // Auto-confirm the reservation now that it's paid
        reservationService.updateStatus(reservationId, "CONFIRMED");
        return saved;
    }

    public Payment processCardPayment(Long reservationId, String fullCardNumber,
                                      String cardholderName, String cardType) {
        Reservation reservation = reservationService.getById(reservationId);
        validatePayable(reservation);

        CardPayment payment = new CardPayment();
        payment.setReservation(reservation);
        payment.setAmount(reservation.getTotalAmount());
        payment.setFullCardNumber(fullCardNumber);
        payment.setCardholderName(cardholderName);
        payment.setCardType(cardType);

        boolean ok = payment.processPayment();  // <-- polymorphic call
        if (!ok) {
            throw new IllegalArgumentException("Card payment failed. Please verify your card details.");
        }

        Payment saved = repository.save(payment);
        reservationService.updateStatus(reservationId, "CONFIRMED");
        return saved;
    }

    private void validatePayable(Reservation reservation) {
        if (repository.existsByReservation(reservation)) {
            throw new IllegalArgumentException("This reservation has already been paid.");
        }
        if ("CANCELLED".equals(reservation.getStatus())) {
            throw new IllegalArgumentException("Cannot pay for a cancelled reservation.");
        }
    }
}