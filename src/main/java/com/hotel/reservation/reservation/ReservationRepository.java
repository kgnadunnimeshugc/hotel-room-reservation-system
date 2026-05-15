package com.hotel.reservation.reservation;

import com.hotel.reservation.person.Customer;
import com.hotel.reservation.room.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByCustomerOrderByBookedOnDesc(Customer customer);

    List<Reservation> findByRoom(Room room);

    // Custom JPQL — finds reservations that overlap with a given date range
    @Query("""
        SELECT r FROM Reservation r 
        WHERE r.room.id = :roomId 
        AND r.status NOT IN ('CANCELLED', 'COMPLETED')
        AND r.checkIn < :checkOut 
        AND r.checkOut > :checkIn
    """)
    List<Reservation> findConflictingReservations(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );
}