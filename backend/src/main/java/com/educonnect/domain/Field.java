package com.educonnect.domain;

import jakarta.persistence.*;
import lombok.*;

/** Domaine d'études (Informatique, Mathématiques, Droit...). */
@Entity
@Table(name = "fields")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String icon;

    public Field(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }
}
