package com.hotel.reservation.person;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Table(name = "staff")
@Inheritance(strategy = InheritanceType.JOINED)
public class Staff extends Person {

    private String position;       // e.g. "Receptionist", "Manager"
    private double salary;

    public Staff() {
        super();
    }

    @Override
    public String getRole() {
        return "STAFF";
    }

    // Staff-specific behavior
    public boolean canCheckInGuests() {
        return true;
    }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }
}