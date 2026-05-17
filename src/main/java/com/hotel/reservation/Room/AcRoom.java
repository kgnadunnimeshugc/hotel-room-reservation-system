package com.hotel.reservation.room;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("AC")
public class AcRoom extends Room {

    private static final double AC_SURCHARGE_RATE = 0.15;  

    public AcRoom() {}

    @Override
    public double calculatePrice(int nights) {
        
        double base = getCategory().getBasePrice() * nights;
        return base + (base * AC_SURCHARGE_RATE);
    }
}
