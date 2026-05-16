package com.hotel.reservation.payment;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CARD")
public class CardPayment extends Payment {

    private String cardLastFour;
    private String cardholderName;
    private String cardType;

    public CardPayment() {
        super();
    }

    @Override
    public boolean processPayment() {

        if (getAmount() <= 0) {
            setStatus("FAILED");
            return false;
        }
        if (cardLastFour == null || cardLastFour.length() != 4) {
            setStatus("FAILED");
            return false;
        }
        if (cardholderName == null || cardholderName.isBlank()) {
            setStatus("FAILED");
            return false;
        }
        setStatus("COMPLETED");
        return true;
    }

    public void setFullCardNumber(String fullNumber) {
        if (fullNumber != null && fullNumber.length() >= 4) {
            this.cardLastFour = fullNumber.substring(fullNumber.length() - 4);
        }
    }

    public String getCardLastFour() { return cardLastFour; }
    public void setCardLastFour(String cardLastFour) { this.cardLastFour = cardLastFour; }

    public String getCardholderName() { return cardholderName; }
    public void setCardholderName(String cardholderName) { this.cardholderName = cardholderName; }

    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }
}