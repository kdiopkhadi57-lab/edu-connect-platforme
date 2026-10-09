package com.educonnect.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Publication : document, vidéo, exercice, sujet d'examen, question ou annonce. */
@Entity
@Table(name = "posts", indexes = {
        @Index(columnList = "field_id,level"),
        @Index(columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType type;

    @Column(nullable = false)
    private String title;

    @Column(length = 20000)
    private String content;

    /** Corrigé d'un exercice / sujet — masqué par défaut côté interface. */
    @Column(length = 20000)
    private String solution;

    @ManyToOne(fetch = FetchType.LAZY)
    private Field field;

    @Enumerated(EnumType.STRING)
    private Level level;

    private String serie;

    @ManyToOne(fetch = FetchType.LAZY)
    private Community community;

    /** Lien vidéo externe (YouTube, Vimeo...) en alternative à un fichier. */
    private String videoUrl;

    /** Mots-clés séparés par des virgules. */
    private String tags;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    private int likeCount;
    private int commentCount;
    private int downloadCount;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
