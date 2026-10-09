package com.educonnect.service;

import com.educonnect.domain.*;
import com.educonnect.repository.*;
import com.educonnect.web.dto.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    private static final int PAGE_SIZE = 15;
    private static final int MAX_FILES = 10;

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FieldRepository fieldRepository;
    private final CommunityRepository communityRepository;
    private final FollowRepository followRepository;
    private final PostLikeRepository likeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;
    private final StorageService storage;

    public PostService(PostRepository postRepository, UserRepository userRepository, FieldRepository fieldRepository,
                       CommunityRepository communityRepository, FollowRepository followRepository,
                       PostLikeRepository likeRepository, BookmarkRepository bookmarkRepository,
                       CommentRepository commentRepository, AttachmentRepository attachmentRepository,
                       StorageService storage) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.fieldRepository = fieldRepository;
        this.communityRepository = communityRepository;
        this.followRepository = followRepository;
        this.likeRepository = likeRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.commentRepository = commentRepository;
        this.attachmentRepository = attachmentRepository;
        this.storage = storage;
    }

    // ---------------------------------------------------------------- création / suppression

    @Transactional
    public PostDto create(Long me, CreatePostRequest req, List<MultipartFile> files) {
        User author = userRepository.findById(me).orElseThrow(() -> new NotFoundException("Utilisateur"));
        List<MultipartFile> uploads = files == null ? List.of() : files.stream().filter(f -> !f.isEmpty()).toList();
        if (uploads.size() > MAX_FILES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum " + MAX_FILES + " fichiers par publication");
        }
        if (req.type() == PostType.VIDEO && blank(req.videoUrl())
                && uploads.stream().noneMatch(f -> f.getContentType() != null && f.getContentType().startsWith("video/"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une vidéo nécessite un fichier vidéo ou un lien");
        }

        Post post = new Post();
        post.setAuthor(author);
        post.setType(req.type());
        post.setTitle(req.title().trim());
        post.setContent(trimToNull(req.content()));
        post.setSolution(trimToNull(req.solution()));
        post.setField(req.fieldId() == null ? author.getField()
                : fieldRepository.findById(req.fieldId()).orElseThrow(() -> new NotFoundException("Domaine")));
        post.setLevel(req.level() == null ? author.getLevel() : req.level());
        post.setSerie(blank(req.serie()) ? null : req.serie().trim().toUpperCase(Locale.ROOT));
        post.setVideoUrl(trimToNull(req.videoUrl()));
        if (req.tags() != null) {
            post.setTags(req.tags().stream().map(String::trim).filter(t -> !t.isEmpty())
                    .map(t -> t.replace(",", " ").toLowerCase(Locale.ROOT)).distinct().limit(10)
                    .collect(Collectors.joining(",")));
        }
        if (req.communityId() != null) {
            Community community = communityRepository.findById(req.communityId())
                    .orElseThrow(() -> new NotFoundException("Communauté"));
            if (!author.getCommunities().contains(community)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'êtes pas membre de cette communauté");
            }
            post.setCommunity(community);
        }
        for (MultipartFile file : uploads) {
            Attachment a = new Attachment();
            a.setPost(post);
            a.setOriginalName(file.getOriginalFilename() == null ? "fichier" : file.getOriginalFilename());
            a.setContentType(file.getContentType());
            a.setSize(file.getSize());
            a.setStoredName(storage.store(file));
            post.getAttachments().add(a);
        }
        postRepository.save(post);
        return toDtos(List.of(post), me).getFirst();
    }

    @Transactional
    public void delete(Long me, Long postId) {
        Post post = load(postId);
        if (!post.getAuthor().getId().equals(me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul l'auteur peut supprimer cette publication");
        }
        likeRepository.deleteByPostId(postId);
        bookmarkRepository.deleteByPostId(postId);
        commentRepository.deleteByPostId(postId);
        post.getAttachments().forEach(a -> storage.delete(a.getStoredName()));
        postRepository.delete(post);
    }

    // ---------------------------------------------------------------- lecture

    @Transactional(readOnly = true)
    public PostDto get(Long me, Long postId) {
        return toDtos(List.of(load(postId)), me).getFirst();
    }

    /** Fil personnalisé : mes communautés + mon domaine/niveau + les personnes suivies + mes posts. */
    @Transactional(readOnly = true)
    public PageDto<PostDto> feed(Long me, int page) {
        User user = userRepository.findById(me).orElseThrow(() -> new NotFoundException("Utilisateur"));
        List<Long> communityIds = new ArrayList<>(user.getCommunities().stream().map(Community::getId).toList());
        List<Long> authorIds = new ArrayList<>(followRepository.findFollowedIds(me));
        authorIds.add(me);
        communityIds.add(-1L); // évite une clause IN vide
        Page<Post> result = postRepository.findFeed(communityIds, user.getField(), user.getLevel(), authorIds,
                PageRequest.of(page, PAGE_SIZE));
        return PageDto.of(result, list -> toDtos(list, me));
    }

    /** Exploration transversale par domaine, niveau, série, type et mots-clés. */
    @Transactional(readOnly = true)
    public PageDto<PostDto> explore(Long me, Long fieldId, Level level, String serie, PostType type, Cycle cycle,
                                    String q, String sort, int page) {
        Specification<Post> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (fieldId != null) p.add(cb.equal(root.get("field").get("id"), fieldId));
            if (level != null) p.add(cb.equal(root.get("level"), level));
            if (cycle != null) {
                p.add(root.get("level").in(Arrays.stream(Level.values()).filter(l -> l.getCycle() == cycle).toList()));
            }
            if (!blank(serie)) p.add(cb.equal(root.get("serie"), serie.trim().toUpperCase(Locale.ROOT)));
            if (type != null) p.add(cb.equal(root.get("type"), type));
            if (!blank(q)) {
                String like = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                p.add(cb.or(cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("content")), like),
                        cb.like(cb.lower(root.get("tags")), like)));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
        Sort order = "popular".equals(sort)
                ? Sort.by(Sort.Order.desc("likeCount"), Sort.Order.desc("downloadCount"), Sort.Order.desc("createdAt"))
                : Sort.by(Sort.Order.desc("createdAt"));
        return PageDto.of(postRepository.findAll(spec, PageRequest.of(page, PAGE_SIZE, order)), list -> toDtos(list, me));
    }

    @Transactional(readOnly = true)
    public PageDto<PostDto> byAuthor(Long me, Long authorId, int page) {
        return PageDto.of(postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, PageRequest.of(page, PAGE_SIZE)),
                list -> toDtos(list, me));
    }

    @Transactional(readOnly = true)
    public PageDto<PostDto> byCommunity(Long me, Long communityId, int page) {
        return PageDto.of(postRepository.findByCommunityIdOrderByCreatedAtDesc(communityId, PageRequest.of(page, PAGE_SIZE)),
                list -> toDtos(list, me));
    }

    @Transactional(readOnly = true)
    public PageDto<PostDto> bookmarks(Long me, int page) {
        return PageDto.of(postRepository.findBookmarked(me, PageRequest.of(page, PAGE_SIZE)), list -> toDtos(list, me));
    }

    // ---------------------------------------------------------------- interactions

    @Transactional
    public void like(Long me, Long postId) {
        if (likeRepository.findByPostIdAndUserId(postId, me).isEmpty()) {
            likeRepository.save(new PostLike(load(postId), userRepository.getReferenceById(me)));
            postRepository.addLikes(postId, 1);
        }
    }

    @Transactional
    public void unlike(Long me, Long postId) {
        likeRepository.findByPostIdAndUserId(postId, me).ifPresent(l -> {
            likeRepository.delete(l);
            postRepository.addLikes(postId, -1);
        });
    }

    @Transactional
    public void bookmark(Long me, Long postId) {
        if (bookmarkRepository.findByPostIdAndUserId(postId, me).isEmpty()) {
            bookmarkRepository.save(new Bookmark(load(postId), userRepository.getReferenceById(me)));
        }
    }

    @Transactional
    public void unbookmark(Long me, Long postId) {
        bookmarkRepository.findByPostIdAndUserId(postId, me).ifPresent(bookmarkRepository::delete);
    }

    @Transactional(readOnly = true)
    public PageDto<CommentDto> comments(Long me, Long postId, int page) {
        return PageDto.of(commentRepository.findByPostIdOrderByCreatedAtAsc(postId, PageRequest.of(page, 50)),
                list -> list.stream().map(c -> CommentDto.of(c, me)).toList());
    }

    @Transactional
    public CommentDto addComment(Long me, Long postId, CreateCommentRequest req) {
        Comment c = new Comment();
        c.setPost(load(postId));
        c.setAuthor(userRepository.findById(me).orElseThrow(() -> new NotFoundException("Utilisateur")));
        c.setContent(req.content().trim());
        commentRepository.save(c);
        postRepository.addComments(postId, 1);
        return CommentDto.of(c, me);
    }

    @Transactional
    public void deleteComment(Long me, Long postId, Long commentId) {
        Comment c = commentRepository.findById(commentId)
                .filter(x -> x.getPost().getId().equals(postId))
                .orElseThrow(() -> new NotFoundException("Commentaire"));
        if (!c.getAuthor().getId().equals(me) && !c.getPost().getAuthor().getId().equals(me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Action non autorisée");
        }
        commentRepository.delete(c);
        postRepository.addComments(postId, -1);
    }

    /** Téléchargement / lecture d'une pièce jointe ; compte les téléchargements. */
    @Transactional
    public FileDownload download(String storedName, boolean countDownload) {
        Attachment a = attachmentRepository.findByStoredName(storedName)
                .orElseThrow(() -> new NotFoundException("Fichier"));
        if (countDownload) {
            postRepository.incrementDownloads(a.getPost().getId());
        }
        return new FileDownload(storage.load(storedName), a.getOriginalName(), a.getContentType());
    }

    public record FileDownload(Resource resource, String originalName, String contentType) {
    }

    // ---------------------------------------------------------------- mapping

    private Post load(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new NotFoundException("Publication"));
    }

    private List<PostDto> toDtos(List<Post> posts, Long me) {
        if (posts.isEmpty()) {
            return List.of();
        }
        List<Long> ids = posts.stream().map(Post::getId).toList();
        Set<Long> liked = likeRepository.findLikedPostIds(me, ids);
        Set<Long> bookmarked = bookmarkRepository.findBookmarkedPostIds(me, ids);
        return posts.stream().map(p -> new PostDto(
                p.getId(), p.getType(), p.getType().getLabel(), p.getTitle(), p.getContent(),
                p.getSolution() != null, p.getSolution(),
                FieldDto.of(p.getField()), p.getLevel(), p.getLevel() == null ? null : p.getLevel().getLabel(), p.getSerie(),
                p.getCommunity() == null ? null : p.getCommunity().getId(),
                p.getCommunity() == null ? null : p.getCommunity().getName(),
                p.getVideoUrl(),
                p.getTags() == null || p.getTags().isBlank() ? List.of() : List.of(p.getTags().split(",")),
                p.getAttachments().stream().map(AttachmentDto::of).toList(),
                UserSummaryDto.of(p.getAuthor()), p.getCreatedAt(),
                p.getLikeCount(), p.getCommentCount(), p.getDownloadCount(),
                liked.contains(p.getId()), bookmarked.contains(p.getId()), p.getAuthor().getId().equals(me)
        )).toList();
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static String trimToNull(String s) {
        return blank(s) ? null : s.trim();
    }
}
