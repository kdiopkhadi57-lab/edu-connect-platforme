package com.educonnect.service;

import com.educonnect.domain.Field;
import com.educonnect.domain.Follow;
import com.educonnect.domain.Level;
import com.educonnect.domain.Cycle;
import com.educonnect.domain.User;
import com.educonnect.repository.FieldRepository;
import com.educonnect.repository.FollowRepository;
import com.educonnect.repository.PostRepository;
import com.educonnect.repository.UserRepository;
import com.educonnect.security.JwtService;
import com.educonnect.web.dto.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FieldRepository fieldRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;
    private final CommunityService communityService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, FieldRepository fieldRepository, FollowRepository followRepository,
                       PostRepository postRepository, CommunityService communityService,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.fieldRepository = fieldRepository;
        this.followRepository = followRepository;
        this.postRepository = postRepository;
        this.communityService = communityService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un compte existe déjà avec cet e-mail");
        }
        User user = new User();
        user.setFullName(req.fullName().trim());
        user.setEmail(req.email().trim().toLowerCase(Locale.ROOT));
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(req.role());
        user.setInstitution(blankToNull(req.institution()));
        user.setField(field(req.fieldId()));
        user.setLevel(req.level());
        user.setSerie(normalizeSerie(req.serie(), req.level()));
        userRepository.save(user);
        communityService.syncAutoCommunities(user, Set.of());
        return new AuthResponse(jwtService.generate(user.getId(), user.getEmail()), toDto(user, user.getId()));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmailIgnoreCase(req.email().trim())
                .filter(u -> passwordEncoder.matches(req.password(), u.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou mot de passe incorrect"));
        return new AuthResponse(jwtService.generate(user.getId(), user.getEmail()), toDto(user, user.getId()));
    }

    @Transactional(readOnly = true)
    public UserDto get(Long me, Long id) {
        return toDto(load(id), me);
    }

    @Transactional
    public UserDto updateProfile(Long me, UpdateProfileRequest req) {
        User user = load(me);
        Set<String> previous = communityService.autoSlugsFor(user);
        user.setFullName(req.fullName().trim());
        user.setRole(req.role());
        user.setInstitution(blankToNull(req.institution()));
        user.setField(field(req.fieldId()));
        user.setLevel(req.level());
        user.setSerie(normalizeSerie(req.serie(), req.level()));
        user.setBio(blankToNull(req.bio()));
        communityService.syncAutoCommunities(user, previous);
        return toDto(user, me);
    }

    @Transactional
    public void follow(Long me, Long id) {
        if (me.equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible de se suivre soi-même");
        }
        if (!followRepository.existsByFollowerIdAndFollowedId(me, id)) {
            followRepository.save(new Follow(load(me), load(id)));
        }
    }

    @Transactional
    public void unfollow(Long me, Long id) {
        followRepository.findByFollowerIdAndFollowedId(me, id).ifPresent(followRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<UserSummaryDto> search(String q) {
        return userRepository.search(q == null ? "" : q.trim(), PageRequest.of(0, 30)).stream()
                .map(UserSummaryDto::of).toList();
    }

    User load(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("Utilisateur"));
    }

    private UserDto toDto(User u, Long me) {
        return UserDto.of(u, u.getId().equals(me),
                postRepository.countByAuthorId(u.getId()),
                followRepository.countByFollowedId(u.getId()),
                followRepository.countByFollowerId(u.getId()),
                !u.getId().equals(me) && followRepository.existsByFollowerIdAndFollowedId(me, u.getId()));
    }

    private Field field(Long id) {
        return fieldRepository.findById(id).orElseThrow(() -> new NotFoundException("Domaine"));
    }

    /** La série n'a de sens qu'au lycée. */
    private static String normalizeSerie(String serie, Level level) {
        if (serie == null || serie.isBlank() || level == null || level.getCycle() != Cycle.LYCEE) {
            return null;
        }
        return serie.trim().toUpperCase(Locale.ROOT);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
