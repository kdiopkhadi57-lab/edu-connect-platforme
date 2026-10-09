package com.educonnect.service;

import com.educonnect.domain.*;
import com.educonnect.repository.CommunityRepository;
import com.educonnect.repository.UserRepository;
import com.educonnect.web.dto.CommunityDto;
import com.educonnect.web.dto.CreateCommunityRequest;
import com.educonnect.web.dto.PageDto;
import com.educonnect.web.dto.UserSummaryDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;

/**
 * Cœur du principe « adoption automatique » : chaque utilisateur est inscrit d'office
 * dans les communautés qui correspondent à son profil (domaine, niveau, série, établissement),
 * quelle que soit son école. Il retrouve ainsi immédiatement des pairs et du contenu pertinent.
 */
@Service
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;

    public CommunityService(CommunityRepository communityRepository, UserRepository userRepository) {
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
    }

    /** Recalcule les communautés automatiques d'un utilisateur à partir de son profil. */
    @Transactional
    public void syncAutoCommunities(User user, Set<String> previousAutoSlugs) {
        Map<String, Community> wanted = new LinkedHashMap<>();
        for (Community c : autoCommunitiesFor(user)) {
            wanted.put(c.getSlug(), c);
        }
        user.getCommunities().removeIf(c -> previousAutoSlugs.contains(c.getSlug()) && !wanted.containsKey(c.getSlug()));
        user.getCommunities().addAll(wanted.values());
    }

    public Set<String> autoSlugsFor(User user) {
        Set<String> slugs = new HashSet<>();
        if (user.getField() != null) {
            slugs.add(fieldSlug(user.getField()));
            if (user.getLevel() != null) {
                slugs.add(fieldLevelSlug(user.getField(), user.getLevel()));
            }
        }
        if (user.getSerie() != null && user.getLevel() != null) {
            slugs.add(serieSlug(user.getSerie(), user.getLevel()));
        }
        if (user.getInstitution() != null && !user.getInstitution().isBlank()) {
            slugs.add(institutionSlug(user.getInstitution()));
        }
        return slugs;
    }

    private List<Community> autoCommunitiesFor(User user) {
        List<Community> result = new ArrayList<>();
        Field field = user.getField();
        Level level = user.getLevel();
        if (field != null) {
            result.add(findOrCreate(fieldSlug(field), CommunityType.FIELD_LEVEL, field.getName() + " — tous niveaux",
                    "Tous les apprenants en " + field.getName() + ", toutes écoles et tous niveaux confondus.",
                    c -> c.setField(field)));
            if (level != null) {
                result.add(findOrCreate(fieldLevelSlug(field, level), CommunityType.FIELD_LEVEL,
                        field.getName() + " · " + level.getLabel(),
                        "Cours, exercices et entraide en " + field.getName() + " niveau " + level.getLabel() + ".",
                        c -> {
                            c.setField(field);
                            c.setLevel(level);
                        }));
            }
        }
        if (user.getSerie() != null && level != null) {
            String serie = user.getSerie();
            result.add(findOrCreate(serieSlug(serie, level), CommunityType.SERIE_LEVEL,
                    "Série " + serie + " · " + level.getLabel(),
                    "Élèves de série " + serie + " en " + level.getLabel() + " de tous les établissements.",
                    c -> {
                        c.setSerie(serie);
                        c.setLevel(level);
                    }));
        }
        if (user.getInstitution() != null && !user.getInstitution().isBlank()) {
            String inst = user.getInstitution().trim();
            result.add(findOrCreate(institutionSlug(inst), CommunityType.INSTITUTION, inst,
                    "Communauté de l'établissement " + inst + ".",
                    c -> c.setInstitution(inst)));
        }
        return result;
    }

    private Community findOrCreate(String slug, CommunityType type, String name, String description,
                                   java.util.function.Consumer<Community> init) {
        return communityRepository.findBySlug(slug).orElseGet(() -> {
            Community c = new Community();
            c.setSlug(slug);
            c.setType(type);
            c.setName(name);
            c.setDescription(description);
            init.accept(c);
            return communityRepository.save(c);
        });
    }

    @Transactional
    public CommunityDto create(Long me, CreateCommunityRequest req) {
        User user = userRepository.findById(me).orElseThrow(() -> new NotFoundException("Utilisateur"));
        Community c = new Community();
        c.setSlug("custom:" + UUID.randomUUID());
        c.setType(CommunityType.CUSTOM);
        c.setName(req.name().trim());
        c.setDescription(req.description());
        communityRepository.save(c);
        user.getCommunities().add(c);
        return CommunityDto.of(c, 1, true);
    }

    @Transactional(readOnly = true)
    public List<CommunityDto> mine(Long me) {
        User user = userRepository.findById(me).orElseThrow(() -> new NotFoundException("Utilisateur"));
        return user.getCommunities().stream()
                .sorted(Comparator.comparing(Community::getType).thenComparing(Community::getName))
                .map(c -> CommunityDto.of(c, userRepository.countMembers(c.getId()), true))
                .toList();
    }

    @Transactional(readOnly = true)
    public CommunityDto get(Long me, Long id) {
        Community c = communityRepository.findById(id).orElseThrow(() -> new NotFoundException("Communauté"));
        User user = userRepository.findById(me).orElseThrow(() -> new NotFoundException("Utilisateur"));
        return CommunityDto.of(c, userRepository.countMembers(id), user.getCommunities().contains(c));
    }

    @Transactional(readOnly = true)
    public List<CommunityDto> search(Long me, String q) {
        User user = userRepository.findById(me).orElseThrow(() -> new NotFoundException("Utilisateur"));
        return communityRepository.search(q == null ? "" : q.trim(), PageRequest.of(0, 30)).stream()
                .map(c -> CommunityDto.of(c, userRepository.countMembers(c.getId()), user.getCommunities().contains(c)))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageDto<UserSummaryDto> members(Long id, int page) {
        return PageDto.of(userRepository.findMembers(id, PageRequest.of(page, 30)),
                list -> list.stream().map(UserSummaryDto::of).toList());
    }

    @Transactional
    public void join(Long me, Long id) {
        User user = userRepository.findById(me).orElseThrow(() -> new NotFoundException("Utilisateur"));
        user.getCommunities().add(communityRepository.findById(id).orElseThrow(() -> new NotFoundException("Communauté")));
    }

    @Transactional
    public void leave(Long me, Long id) {
        User user = userRepository.findById(me).orElseThrow(() -> new NotFoundException("Utilisateur"));
        user.getCommunities().removeIf(c -> c.getId().equals(id));
    }

    private static String fieldSlug(Field f) {
        return "field:" + f.getId();
    }

    private static String fieldLevelSlug(Field f, Level l) {
        return "field:" + f.getId() + "|level:" + l.name();
    }

    private static String serieSlug(String serie, Level l) {
        return "serie:" + serie + "|level:" + l.name();
    }

    private static String institutionSlug(String institution) {
        String n = Normalizer.normalize(institution.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9]+", "-");
        return "inst:" + n;
    }
}
