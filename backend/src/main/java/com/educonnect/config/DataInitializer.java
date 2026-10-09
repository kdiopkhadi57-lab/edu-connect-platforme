package com.educonnect.config;

import com.educonnect.domain.*;
import com.educonnect.repository.FieldRepository;
import com.educonnect.repository.PostRepository;
import com.educonnect.repository.UserRepository;
import com.educonnect.service.CommunityService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final List<String[]> FIELDS = List.of(
            new String[]{"Informatique", "💻"}, new String[]{"Mathématiques", "📐"},
            new String[]{"Physique-Chimie", "⚗️"}, new String[]{"Sciences de la vie et de la Terre", "🧬"},
            new String[]{"Médecine & Santé", "🩺"}, new String[]{"Économie & Gestion", "📊"},
            new String[]{"Comptabilité & Finance", "💰"}, new String[]{"Droit & Sciences politiques", "⚖️"},
            new String[]{"Lettres & Philosophie", "📚"}, new String[]{"Langues étrangères", "🌍"},
            new String[]{"Histoire-Géographie", "🗺️"}, new String[]{"Génie civil & BTP", "🏗️"},
            new String[]{"Électrotechnique & Électronique", "🔌"}, new String[]{"Mécanique & Maintenance", "🔧"},
            new String[]{"Commerce & Marketing", "🛒"}, new String[]{"Agriculture & Agronomie", "🌾"},
            new String[]{"Hôtellerie & Tourisme", "🏨"}, new String[]{"Communication & Journalisme", "📰"},
            new String[]{"Arts & Design", "🎨"}, new String[]{"Réseaux & Télécoms", "📡"});

    private final FieldRepository fieldRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommunityService communityService;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties props;

    public DataInitializer(FieldRepository fieldRepository, UserRepository userRepository, PostRepository postRepository,
                           CommunityService communityService, PasswordEncoder passwordEncoder, AppProperties props) {
        this.fieldRepository = fieldRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.communityService = communityService;
        this.passwordEncoder = passwordEncoder;
        this.props = props;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (fieldRepository.count() == 0) {
            FIELDS.forEach(f -> fieldRepository.save(new Field(f[0], f[1])));
        }
        if (props.seedDemoData() && userRepository.count() == 0) {
            seedDemo();
        }
    }

    private void seedDemo() {
        Field info = fieldRepository.findByName("Informatique").orElseThrow();
        Field maths = fieldRepository.findByName("Mathématiques").orElseThrow();
        Field elec = fieldRepository.findByName("Électrotechnique & Électronique").orElseThrow();

        User awa = user("Awa Diop", "awa@demo.sn", Role.ETUDIANT, "Université Cheikh Anta Diop", info, Level.L2, null);
        User moussa = user("Moussa Ndiaye", "moussa@demo.sn", Role.ETUDIANT, "Université Gaston Berger", info, Level.L2, null);
        User fatou = user("Fatou Sarr", "fatou@demo.sn", Role.ELEVE, "Lycée Seydina Limamou Laye", maths, Level.TERMINALE, "S2");
        User ibrahima = user("Ibrahima Fall", "ibrahima@demo.sn", Role.APPRENANT_PRO, "Centre de formation pro de Thiès", elec, Level.BTS_1, null);
        User prof = user("M. Sow", "prof@demo.sn", Role.ENSEIGNANT, "Université Cheikh Anta Diop", info, Level.L2, null);

        post(prof, PostType.DOCUMENT, "Cours complet : Structures de données (listes, piles, files)",
                "Support de cours de L2 avec exemples en Java. N'hésitez pas à poser vos questions en commentaire.",
                null, info, Level.L2, null, "algorithmique,java,structures de données");
        post(awa, PostType.EXERCICE, "Exercice : complexité d'un tri par insertion",
                "Calculer la complexité dans le meilleur et le pire des cas du tri par insertion. Justifier.",
                "Meilleur cas (tableau trié) : O(n). Pire cas (tableau trié à l'envers) : O(n²), car chaque élément est comparé à tous les précédents.",
                info, Level.L2, null, "algorithmique,complexité");
        post(moussa, PostType.QUESTION, "Quelqu'un a des annales de bases de données (SQL) ?",
                "Je prépare l'examen de BD de L2, je cherche des sujets corrigés sur les jointures et la normalisation.",
                null, info, Level.L2, null, "sql,bases de données,examen");
        post(fatou, PostType.EPREUVE, "Bac S2 — Sujet de Mathématiques (nombres complexes)",
                "Exercice 1 : Résoudre dans ℂ l'équation z² − 2z + 5 = 0, puis placer les solutions dans le plan complexe.",
                "Δ = 4 − 20 = −16 = (4i)². Solutions : z₁ = 1 + 2i et z₂ = 1 − 2i (symétriques par rapport à l'axe réel).",
                maths, Level.TERMINALE, "S2", "bac,complexes");
        post(ibrahima, PostType.DOCUMENT, "Fiche TP : câblage d'un démarrage étoile-triangle",
                "Schéma de puissance et de commande, liste du matériel et étapes de vérification pour le TP de BTS.",
                null, elec, Level.BTS_1, null, "moteur,tp,câblage");
    }

    private User user(String name, String email, Role role, String institution, Field field, Level level, String serie) {
        User u = new User();
        u.setFullName(name);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode("password123"));
        u.setRole(role);
        u.setInstitution(institution);
        u.setField(field);
        u.setLevel(level);
        u.setSerie(serie);
        userRepository.save(u);
        communityService.syncAutoCommunities(u, Set.of());
        return u;
    }

    private Post post(User author, PostType type, String title, String content, String solution,
                      Field field, Level level, String serie, String tags) {
        Post p = new Post();
        p.setAuthor(author);
        p.setType(type);
        p.setTitle(title);
        p.setContent(content);
        p.setSolution(solution);
        p.setField(field);
        p.setLevel(level);
        p.setSerie(serie);
        p.setTags(tags);
        return postRepository.save(p);
    }
}
