package com.hotel.reservation.room;

import com.hotel.reservation.roomcategory.RoomCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "rooms")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "room_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Room number is required")
    @Column(nullable = false, unique = true)
    private String roomNumber;

    private int floor;

    @Column(nullable = false)
    private String status = "AVAILABLE";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private RoomCategory category;

    public Room() {}

    // Polymorphic method — every subclass MUST override this
    public abstract double calculatePrice(int nights);

    // Concrete method — shared by all subclasses
    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(this.status);
    }

    @Transient
    public String getRoomType() {
        return this.getClass().getSimpleName();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public RoomCategory getCategory() { return category; }
    public void setCategory(RoomCategory category) { this.category = category; }
}