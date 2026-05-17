package com.hotel.reservation.room;

import com.hotel.reservation.roomcategory.RoomCategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;
    private final RoomCategoryService categoryService;

    public RoomController(RoomService roomService, RoomCategoryService categoryService) {
        this.roomService = roomService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("rooms", roomService.getAll());
        return "room/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("roomTypes", new String[]{"NON_AC", "AC", "FAMILY"});
        return "room/form";
    }

    @PostMapping("/save")
    public String save(@RequestParam String roomType,
                       @RequestParam String roomNumber,
                       @RequestParam int floor,
                       @RequestParam String status,
                       @RequestParam Long categoryId,
                       @RequestParam(required = false) Long id,
                       org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {
        try {
            var category = categoryService.getById(categoryId);

            Room room;
            if (id != null) {
                room = roomService.getById(id);
                room.setRoomNumber(roomNumber);
                room.setFloor(floor);
                room.setStatus(status);
                room.setCategory(category);
            } else {
                room = roomService.createRoomOfType(roomType, roomNumber, floor, status, category);
            }

            roomService.save(room);
            return "redirect:/rooms";

        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
            return id != null ? "redirect:/rooms/edit/" + id : "redirect:/rooms/new";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("room", roomService.getById(id));
        model.addAttribute("categories", categoryService.getAll());
        return "room/edit";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        roomService.delete(id);
        return "redirect:/rooms";
    }

}