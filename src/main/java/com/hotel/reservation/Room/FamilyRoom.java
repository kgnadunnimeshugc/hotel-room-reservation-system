package com.hotel.reservation.room;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("FAMILY")
public class FamilyRoom extends Room {

    private static final double EXTRA_BED_FEE_PER_NIGHT = 800.0;

    @Column(name = "extra_beds")
    private int extraBeds = 1;

    public FamilyRoom() {}

    @Override
    public double calculatePrice(int nights) {
        double base = getCategory().getBasePrice() * nights;
        double extraBedCost = EXTRA_BED_FEE_PER_NIGHT * extraBeds * nights;
        return base + extraBedCost;
    }

    public int getExtraBeds() { return extraBeds; }
    public void setExtraBeds(int extraBeds) { this.extraBeds = extraBeds; }
}