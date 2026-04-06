package com.studyplanner.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("TUTOR")
public class Tutor extends User {

    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Intervention> interventions = new ArrayList<>();

    public Tutor() {
    }

    public Tutor(String username, String passwordHash, String email) {
        super(username, passwordHash, email, UserRole.TUTOR);
    }

    public List<Intervention> getInterventions() {
        return interventions;
    }
}
