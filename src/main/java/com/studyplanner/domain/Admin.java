package com.studyplanner.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {

    public Admin() {
    }

    public Admin(String username, String passwordHash, String email) {
        super(username, passwordHash, email, UserRole.ADMIN);
    }
}
