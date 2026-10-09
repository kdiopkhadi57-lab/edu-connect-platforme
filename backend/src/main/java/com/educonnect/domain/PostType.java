package com.educonnect.domain;

public enum PostType {
    DOCUMENT("Document / Cours"),
    VIDEO("Vidéo"),
    EXERCICE("Exercice"),
    EPREUVE("Sujet d'examen"),
    QUESTION("Question"),
    ANNONCE("Annonce");

    private final String label;

    PostType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
