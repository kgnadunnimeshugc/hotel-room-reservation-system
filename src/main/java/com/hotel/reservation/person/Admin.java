package com.hotel.reservation.person;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "admins")
public class Admin extends Staff {

    private int adminLevel = 1;   // 1 = standard admin, 2 = super admin

    public Admin() {
        super();
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    // Admin-specific privileges
    public boolean canManageStaff() {
        return true;
    }

    public boolean canDeleteCategories() {
        return adminLevel >= 1;
    }

    public boolean canResetSystem() {
        return adminLevel == 2;
    }

    public int getAdminLevel() { return adminLevel; }
    public void setAdminLevel(int adminLevel) { this.adminLevel = adminLevel; }
}