package com.hotel.reservation.person;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.hotel.reservation.reservation.ReservationService;
import com.hotel.reservation.reservation.Reservation;
import com.hotel.reservation.payment.PaymentService;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final StaffService staffService;
    private final CustomerService customerService;
    private final ReservationService reservationService;
    private final PaymentService paymentService;

    public AdminController(StaffService staffService,
                           CustomerService customerService,
                           ReservationService reservationService,
                           PaymentService paymentService) {
        this.staffService = staffService;
        this.customerService = customerService;
        this.reservationService = reservationService;
        this.paymentService = paymentService;
    }

    // ===== ADMIN LOGIN =====

    @GetMapping("/login")
    public String loginForm() {
        return "admin/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttrs) {
        Optional<Staff> staff = staffService.authenticate(email, password);
        if (staff.isPresent() && ("ADMIN".equals(staff.get().getRole()) || "STAFF".equals(staff.get().getRole()))) {
            session.setAttribute("loggedInStaff", staff.get());
            return "redirect:/admin/dashboard";
        }
        redirectAttrs.addFlashAttribute("errorMessage", "Invalid credentials or not an admin/staff account.");
        return "redirect:/admin/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    // ===== ADMIN DASHBOARD =====

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Staff staff = (Staff) session.getAttribute("loggedInStaff");
        if (staff == null) {
            return "redirect:/admin/login";
        }
        model.addAttribute("staff", staff);
        model.addAttribute("totalCustomers", customerService.getAll().size());
        model.addAttribute("totalStaff", staffService.getAllStaff().size());
        model.addAttribute("totalReservations", reservationService.getAll().size());  // NEW
        return "admin/dashboard";
    }

    // ===== STAFF MANAGEMENT (admin-only) =====

    @GetMapping("/staff")
    public String staffList(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        model.addAttribute("staffList", staffService.getAllStaff());
        return "admin/staff-list";
    }

    @GetMapping("/staff/new")
    public String newStaffForm(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        model.addAttribute("staff", new Staff());
        return "admin/staff-form";
    }

    @PostMapping("/staff/save")
    public String saveStaff(@ModelAttribute Staff staff,
                            HttpSession session,
                            RedirectAttributes redirectAttrs) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        try {
            staffService.registerStaff(staff);
            redirectAttrs.addFlashAttribute("successMessage", "Staff member added.");
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/staff";
    }

    @GetMapping("/staff/delete/{id}")
    public String deleteStaff(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        staffService.deleteStaff(id);
        return "redirect:/admin/staff";
    }

    // ===== CUSTOMER MANAGEMENT (view-only by staff, full by admin) =====

    @GetMapping("/customers")
    public String customerList(HttpSession session, Model model) {
        if (session.getAttribute("loggedInStaff") == null) return "redirect:/admin/login";
        model.addAttribute("customers", customerService.getAll());
        model.addAttribute("isAdmin", isAdmin(session));
        return "admin/customer-list";
    }

    @GetMapping("/customers/delete/{id}")
    public String deleteCustomer(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        customerService.delete(id);
        return "redirect:/admin/customers";
    }

    @GetMapping("/reservations")
    public String reservationList(HttpSession session, Model model) {
        if (session.getAttribute("loggedInStaff") == null) return "redirect:/admin/login";
        model.addAttribute("reservations", reservationService.getAll());
        return "admin/reservation-list";
    }

    @PostMapping("/reservations/status/{id}")
    public String updateReservationStatus(@PathVariable Long id,
                                          @RequestParam String status,
                                          HttpSession session,
                                          RedirectAttributes redirectAttrs) {
        if (session.getAttribute("loggedInStaff") == null) return "redirect:/admin/login";
        try {
            reservationService.updateStatus(id, status);
            redirectAttrs.addFlashAttribute("successMessage", "Reservation status updated to " + status);
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/reservations";
    }

    @GetMapping("/payments")
    public String paymentList(HttpSession session, Model model) {
        if (session.getAttribute("loggedInStaff") == null) return "redirect:/admin/login";
        model.addAttribute("payments", paymentService.getAll());
        return "admin/payment-list";
    }

    // ===== HELPER =====

    private boolean isAdmin(HttpSession session) {
        Staff staff = (Staff) session.getAttribute("loggedInStaff");
        return staff != null && "ADMIN".equals(staff.getRole());
    }
}