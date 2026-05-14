package com.hotel.reservation.room;

import com.hotel.reservation.roomcategory.RoomCategory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository repository;

    public RoomService(RoomRepository repository) {
        this.repository = repository;
    }

    public List<Room> getAll() {
        return repository.findAll();
    }

    public Room getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found: " + id));
    }

    public Room save(Room room) {
        return repository.save(room);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    // Factory method — creates the right subclass based on type string
    public Room createRoomOfType(String type, String roomNumber, int floor,
                                 String status, RoomCategory category) {
        validateTypeMatchesCategory(type, category);  // <-- ADD THIS LINE

        Room room;
        switch (type.toUpperCase()) {
            case "NON_AC" -> room = new NonAcRoom();
            case "AC"     -> room = new AcRoom();
            case "FAMILY" -> room = new FamilyRoom();
            default -> throw new IllegalArgumentException("Unknown room type: " + type);
        }
        room.setRoomNumber(roomNumber);
        room.setFloor(floor);
        room.setStatus(status);
        room.setCategory(category);
        return room;
    }

    private void validateTypeMatchesCategory(String roomType, RoomCategory category) {
        String categoryName = category.getName().toLowerCase();
        String type = roomType.toUpperCase();

        boolean isAcCategory = categoryName.contains("ac") && !categoryName.contains("non");
        boolean isNonAcCategory = categoryName.contains("non-ac") || categoryName.contains("non ac");
        boolean isFamilyCategory = categoryName.contains("family");

        boolean mismatch = false;
        if (type.equals("AC") && !isAcCategory) mismatch = true;
        if (type.equals("NON_AC") && !isNonAcCategory) mismatch = true;
        if (type.equals("FAMILY") && !isFamilyCategory) mismatch = true;

        if (mismatch) {
            throw new IllegalArgumentException(
                    "Room type '" + type + "' does not match category '" + category.getName() + "'. " +
                            "Please select a matching pair."
            );
        }
    }
}