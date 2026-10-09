package com.educonnect.web;

import com.educonnect.domain.Level;
import com.educonnect.domain.PostType;
import com.educonnect.domain.Role;
import com.educonnect.repository.FieldRepository;
import com.educonnect.web.dto.FieldDto;
import com.educonnect.web.dto.LabeledValue;
import com.educonnect.web.dto.ReferenceDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/** Référentiels : domaines, niveaux, séries, rôles, types de publication. */
@RestController
@RequestMapping("/api/reference")
public class ReferenceController {

    private static final List<String> SERIES = List.of(
            "S1", "S2", "S3", "S4", "S5", "L1", "L2", "L'1", "G", "T1", "T2", "STEG",
            "GENERALE", "STMG", "STI2D", "ST2S", "BAC PRO");

    private final FieldRepository fieldRepository;

    public ReferenceController(FieldRepository fieldRepository) {
        this.fieldRepository = fieldRepository;
    }

    @GetMapping
    public ReferenceDto all() {
        return new ReferenceDto(
                fieldRepository.findAllByOrderByNameAsc().stream().map(FieldDto::of).toList(),
                Arrays.stream(Level.values()).map(l -> new LabeledValue(l.name(), l.getLabel(), l.getCycle().getLabel())).toList(),
                SERIES,
                Arrays.stream(Role.values()).map(r -> new LabeledValue(r.name(), r.getLabel(), null)).toList(),
                Arrays.stream(PostType.values()).map(t -> new LabeledValue(t.name(), t.getLabel(), null)).toList());
    }
}
