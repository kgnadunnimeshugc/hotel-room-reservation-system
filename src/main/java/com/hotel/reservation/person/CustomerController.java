package com.hotel.reservation.person;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // ===== REGISTRATION =====

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("customer") Customer customer,
                           BindingResult result,
                           RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            customerService.register(customer);
            redirectAttrs.addFlashAttribute("successMessage", "Account created. Please log in.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }

    // ===== LOGIN =====

    @GetMapping("/login")
    public String loginForm() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttrs) {
        Optional<Customer> customer = customerService.authenticate(email, password);
        if (customer.isPresent()) {
            session.setAttribute("loggedInCustomer", customer.get());
            return "redirect:/profile";
        }
        redirectAttrs.addFlashAttribute("errorMessage", "Invalid email or password.");
        return "redirect:/login";
    }

    // ===== LOGOUT =====

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ===== PROFILE =====

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login";
        }
        // Re-fetch from DB to get fresh data
        customer = customerService.getById(customer.getId());
        model.addAttribute("customer", customer);
        return "auth/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String name,
                                @RequestParam String phone,
                                HttpSession session,
                                RedirectAttributes redirectAttrs) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login";
        }
        customer = customerService.getById(customer.getId());
        customer.setName(name);
        customer.setPhone(phone);
        customerService.save(customer);
        session.setAttribute("loggedInCustomer", customer);
        redirectAttrs.addFlashAttribute("successMessage", "Profile updated.");
        return "redirect:/profile";
    }
}
