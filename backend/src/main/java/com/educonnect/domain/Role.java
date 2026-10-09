package com.educonnect.domain;

public enum Role {
    ELEVE("Élève"),
    ETUDIANT("Étudiant"),
    APPRENANT_PRO("Apprenant en formation pro"),
    ENSEIGNANT("Enseignant / Formateur");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
