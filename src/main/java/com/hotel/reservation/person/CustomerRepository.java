package com.hotel.reservation.person;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository  extends JpaRepository<Customer, Long> {
    // Spring Data JPA auto-generates the SQL from the method name!
    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);
}
