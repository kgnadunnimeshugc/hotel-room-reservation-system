package com.hotel.reservation.roomcategory;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RoomCategoryService {

    private final RoomCategoryRepository repository;

    public RoomCategoryService(RoomCategoryRepository repository) {
        this.repository = repository;
    }

    public List<RoomCategory> getAll() {
        return repository.findAll();
    }

    public RoomCategory getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
    }

    public RoomCategory save(RoomCategory category) {
        return repository.save(category);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}