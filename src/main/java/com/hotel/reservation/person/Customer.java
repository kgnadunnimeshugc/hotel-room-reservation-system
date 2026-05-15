package com.hotel.reservation.person;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer extends Person{
    private int loyaltyPoints = 0;

    public Customer() {
        super();
    }

    @Override
    public String getRole() {
        return "CUSTOMER";
    }

    // Customer-specific behavior
    public void addLoyaltyPoints(int points) {
        this.loyaltyPoints += points;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) {

    }
}
