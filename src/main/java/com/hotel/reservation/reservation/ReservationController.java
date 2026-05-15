package com.hotel.reservation.reservation;

import com.hotel.reservation.person.Customer;
import com.hotel.reservation.room.Room;
import com.hotel.reservation.room.RoomService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/booking")
public class ReservationController {

    private final ReservationService reservationService;
    private final RoomService roomService;

    public ReservationController(ReservationService reservationService, RoomService roomService) {
        this.reservationService = reservationService;
        this.roomService = roomService;
    }

    // Show available rooms for booking
    @GetMapping("/rooms")
    public String browseRooms(HttpSession session, Model model) {
        if (session.getAttribute("loggedInCustomer") == null) {
            return "redirect:/login";
        }
        List<Room> rooms = roomService.getAll().stream()
                .filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus()))
                .toList();

        // Build a map of roomId → hasUpcomingBookings
        java.util.Map<Long, Boolean> bookingStatus = new java.util.HashMap<>();
        for (Room r : rooms) {
            bookingStatus.put(r.getId(), reservationService.hasUpcomingBookings(r.getId()));
        }

        model.addAttribute("rooms", rooms);
        model.addAttribute("bookingStatus", bookingStatus);
        return "booking/browse";
    }

    // Show booking form for a specific room
    @GetMapping("/book/{roomId}")
    public String bookForm(@PathVariable Long roomId, HttpSession session, Model model) {
        if (session.getAttribute("loggedInCustomer") == null) {
            return "redirect:/login";
        }
        Room room = roomService.getById(roomId);
        model.addAttribute("room", room);
        model.addAttribute("today", LocalDate.now().toString());
        model.addAttribute("tomorrow", LocalDate.now().plusDays(1).toString());
        model.addAttribute("existingBookings", reservationService.getActiveBookingsForRoom(roomId));
        return "booking/form";
    }

    // Submit booking
    @PostMapping("/book")
    public String submitBooking(@RequestParam Long roomId,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
                                HttpSession session,
                                RedirectAttributes redirectAttrs) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login";
        }

        try {
            Reservation reservation = reservationService.book(customer, roomId, checkIn, checkOut);
            redirectAttrs.addFlashAttribute("successMessage",
                    "Booking confirmed! Total: Rs. " + reservation.getTotalAmount());
            return "redirect:/booking/confirmation/" + reservation.getId();
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/booking/book/" + roomId;
        }
    }

    // Confirmation page after booking
    @GetMapping("/confirmation/{id}")
    public String confirmation(@PathVariable Long id, HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login";
        }
        Reservation reservation = reservationService.getById(id);
        // Make sure the customer can only see their own confirmation
        if (!reservation.getCustomer().getId().equals(customer.getId())) {
            return "redirect:/profile";
        }
        model.addAttribute("reservation", reservation);
        return "booking/confirmation";
    }

    // Customer's own bookings
    @GetMapping("/my-bookings")
    public String myBookings(HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login";
        }
        List<Reservation> bookings = reservationService.getByCustomer(customer);
        model.addAttribute("bookings", bookings);
        model.addAttribute("customer", customer);
        return "booking/my-bookings";
    }

    // Customer cancels their own booking
    @PostMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id,
                                HttpSession session,
                                RedirectAttributes redirectAttrs) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login";
        }
        try {
            Reservation r = reservationService.getById(id);
            // Customer can only cancel their own bookings
            if (!r.getCustomer().getId().equals(customer.getId())) {
                redirectAttrs.addFlashAttribute("errorMessage", "You can only cancel your own bookings.");
                return "redirect:/booking/my-bookings";
            }
            reservationService.cancel(id);
            redirectAttrs.addFlashAttribute("successMessage", "Booking cancelled.");
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/booking/my-bookings";
    }
}