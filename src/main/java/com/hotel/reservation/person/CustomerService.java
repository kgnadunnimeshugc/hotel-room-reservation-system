package com.hotel.reservation.person;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {

        this.repository = repository;
    }

    public List<Customer> getAll() {

        return repository.findAll();
    }

    public Customer getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));
    }

    public Customer register(Customer customer) {
        if (repository.existsByEmail(customer.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        return repository.save(customer);
    }

    public Customer save(Customer customer) {

        return repository.save(customer);
    }

    public Optional<Customer> authenticate(String email, String password) {
        Optional<Customer> customer = repository.findByEmail(email);
        if (customer.isPresent() && customer.get().login(email, password)) {
            return customer;
        }
        return Optional.empty();
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

}
