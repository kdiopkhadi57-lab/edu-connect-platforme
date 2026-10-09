package com.educonnect.domain;

public enum Level {
    SECONDE("Seconde", Cycle.LYCEE),
    PREMIERE("Première", Cycle.LYCEE),
    TERMINALE("Terminale", Cycle.LYCEE),
    CAP("CAP", Cycle.FORMATION_PRO),
    BEP("BEP", Cycle.FORMATION_PRO),
    BT("BT", Cycle.FORMATION_PRO),
    BTS_1("BTS 1re année", Cycle.FORMATION_PRO),
    BTS_2("BTS 2e année", Cycle.FORMATION_PRO),
    CERTIFICATION("Certification professionnelle", Cycle.FORMATION_PRO),
    FORMATION_CONTINUE("Formation continue", Cycle.FORMATION_PRO),
    L1("Licence 1", Cycle.SUPERIEUR),
    L2("Licence 2", Cycle.SUPERIEUR),
    L3("Licence 3", Cycle.SUPERIEUR),
    M1("Master 1", Cycle.SUPERIEUR),
    M2("Master 2", Cycle.SUPERIEUR),
    INGENIEUR("Cycle ingénieur", Cycle.SUPERIEUR),
    DOCTORAT("Doctorat", Cycle.SUPERIEUR);

    private final String label;
    private final Cycle cycle;

    Level(String label, Cycle cycle) {
        this.label = label;
        this.cycle = cycle;
    }

    public String getLabel() {
        return label;
    }

    public Cycle getCycle() {
        return cycle;
    }
}
