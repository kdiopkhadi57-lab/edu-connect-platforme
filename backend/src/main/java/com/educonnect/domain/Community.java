package com.educonnect.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "communities")
@Getter
@Setter
@NoArgsConstructor
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Clé technique unique pour les communautés automatiques (ex. "field:3|level:L2"). */
    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunityType type;

    @ManyToOne(fetch = FetchType.LAZY)
    private Field field;

    @Enumerated(EnumType.STRING)
    private Level level;

    private String serie;

    private String institution;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
