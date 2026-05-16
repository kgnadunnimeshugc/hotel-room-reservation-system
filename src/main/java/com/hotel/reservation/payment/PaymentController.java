package com.hotel.reservation.payment;

import com.hotel.reservation.person.Customer;
import com.hotel.reservation.reservation.Reservation;
import com.hotel.reservation.reservation.ReservationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final ReservationService reservationService;

    public PaymentController(PaymentService paymentService, ReservationService reservationService) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
    }

    @GetMapping("/checkout/{reservationId}")
    public String checkout(@PathVariable Long reservationId, HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) return "redirect:/login";

        Reservation reservation = reservationService.getById(reservationId);
        if (!reservation.getCustomer().getId().equals(customer.getId())) {
            return "redirect:/profile";
        }

        // Already paid?
        if (paymentService.isPaid(reservation)) {
            Payment existing = paymentService.getByReservation(reservation);
            return "redirect:/payment/receipt/" + existing.getId();
        }

        model.addAttribute("reservation", reservation);
        return "payment/checkout";
    }

    @PostMapping("/cash")
    public String payCash(@RequestParam Long reservationId,
                          @RequestParam String receivedBy,
                          HttpSession session,
                          RedirectAttributes redirectAttrs) {
        if (session.getAttribute("loggedInCustomer") == null) return "redirect:/login";
        try {
            Payment p = paymentService.processCashPayment(reservationId, receivedBy);
            return "redirect:/payment/receipt/" + p.getId();
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/payment/checkout/" + reservationId;
        }
    }

    @PostMapping("/card")
    public String payCard(@RequestParam Long reservationId,
                          @RequestParam String cardNumber,
                          @RequestParam String cardholderName,
                          @RequestParam String cardType,
                          HttpSession session,
                          RedirectAttributes redirectAttrs) {
        if (session.getAttribute("loggedInCustomer") == null) return "redirect:/login";
        try {
            Payment p = paymentService.processCardPayment(reservationId, cardNumber, cardholderName, cardType);
            return "redirect:/payment/receipt/" + p.getId();
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/payment/checkout/" + reservationId;
        }
    }

    @GetMapping("/receipt/{id}")
    public String receipt(@PathVariable Long id, HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) return "redirect:/login";

        Payment payment = paymentService.getById(id);

        if (!payment.getReservation().getCustomer().getId().equals(customer.getId())) {
            return "redirect:/profile";
        }

        model.addAttribute("payment", payment);
        return "payment/receipt";
    }
}