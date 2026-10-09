package com.educonnect.domain;

public enum CommunityType {
    /** Même domaine + même niveau, toutes écoles confondues (ex. Informatique · Licence 2). */
    FIELD_LEVEL,
    /** Même série + même niveau au lycée (ex. Série S2 · Terminale). */
    SERIE_LEVEL,
    /** Même établissement. */
    INSTITUTION,
    /** Communauté créée librement par un utilisateur. */
    CUSTOM
}
