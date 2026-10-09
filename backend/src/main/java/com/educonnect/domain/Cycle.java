package com.educonnect.domain;

public enum Cycle {
    LYCEE("Lycée"),
    FORMATION_PRO("Formation professionnelle"),
    SUPERIEUR("Enseignement supérieur");

    private final String label;

    Cycle(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
