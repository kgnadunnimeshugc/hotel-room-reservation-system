package com.hotel.reservation.reservation;

import com.hotel.reservation.person.Customer;
import com.hotel.reservation.room.Room;
import com.hotel.reservation.room.RoomService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository repository;
    private final RoomService roomService;

    public ReservationService(ReservationRepository repository, RoomService roomService) {
        this.repository = repository;
        this.roomService = roomService;
    }

    public List<Reservation> getAll() {
        return repository.findAll();
    }

    public Reservation getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + id));
    }

    public List<Reservation> getByCustomer(Customer customer) {
        return repository.findByCustomerOrderByBookedOnDesc(customer);
    }

    public Reservation book(Customer customer, Long roomId, LocalDate checkIn, LocalDate checkOut) {
        // Validation: dates make sense
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Both check-in and check-out dates are required.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out must be after check-in.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in cannot be in the past.");
        }

        Room room = roomService.getById(roomId);

        // Validation: no conflicts with existing bookings
        List<Reservation> conflicts = repository.findConflictingReservations(roomId, checkIn, checkOut);
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException(
                    "This room is already booked for some of the selected dates. Please choose different dates."
            );
        }

        // Create the reservation
        Reservation reservation = new Reservation();
        reservation.setCustomer(customer);
        reservation.setRoom(room);
        reservation.setCheckIn(checkIn);
        reservation.setCheckOut(checkOut);
        reservation.setStatus("PENDING");
        reservation.calculateTotal();   // <-- polymorphic call here

        return repository.save(reservation);
    }

    public Reservation updateStatus(Long id, String newStatus) {
        Reservation r = getById(id);
        r.setStatus(newStatus);
        return repository.save(r);
    }

    public void cancel(Long id) {
        Reservation r = getById(id);
        if ("COMPLETED".equals(r.getStatus()) || "CHECKED_IN".equals(r.getStatus())) {
            throw new IllegalArgumentException("Cannot cancel a reservation that is checked-in or completed.");
        }
        r.setStatus("CANCELLED");
        repository.save(r);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public boolean hasUpcomingBookings(Long roomId) {
        LocalDate today = LocalDate.now();
        LocalDate farFuture = today.plusYears(1);
        List<Reservation> bookings = repository.findConflictingReservations(roomId, today, farFuture);
        return !bookings.isEmpty();
    }

    public List<Reservation> getActiveBookingsForRoom(Long roomId) {
        LocalDate today = LocalDate.now();
        LocalDate farFuture = today.plusYears(1);
        return repository.findConflictingReservations(roomId, today, farFuture);
    }

}