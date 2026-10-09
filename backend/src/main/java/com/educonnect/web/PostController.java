package com.educonnect.web;

import com.educonnect.domain.Cycle;
import com.educonnect.domain.Level;
import com.educonnect.domain.PostType;
import com.educonnect.security.CurrentUser;
import com.educonnect.service.PostService;
import com.educonnect.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /** Fil « Pour toi ». */
    @GetMapping("/feed")
    public PageDto<PostDto> feed(@RequestParam(defaultValue = "0") int page) {
        return postService.feed(CurrentUser.id(), page);
    }

    /** Exploration avec filtres (domaine, niveau, cycle, série, type, recherche, tri). */
    @GetMapping
    public PageDto<PostDto> explore(@RequestParam(required = false) Long fieldId,
                                    @RequestParam(required = false) Level level,
                                    @RequestParam(required = false) Cycle cycle,
                                    @RequestParam(required = false) String serie,
                                    @RequestParam(required = false) PostType type,
                                    @RequestParam(required = false) String q,
                                    @RequestParam(defaultValue = "recent") String sort,
                                    @RequestParam(defaultValue = "0") int page) {
        return postService.explore(CurrentUser.id(), fieldId, level, serie, type, cycle, q, sort, page);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PostDto create(@Valid @RequestPart("data") CreatePostRequest data,
                          @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return postService.create(CurrentUser.id(), data, files);
    }

    @GetMapping("/{id}")
    public PostDto get(@PathVariable Long id) {
        return postService.get(CurrentUser.id(), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        postService.delete(CurrentUser.id(), id);
    }

    @PostMapping("/{id}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void like(@PathVariable Long id) {
        postService.like(CurrentUser.id(), id);
    }

    @DeleteMapping("/{id}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlike(@PathVariable Long id) {
        postService.unlike(CurrentUser.id(), id);
    }

    @PostMapping("/{id}/bookmark")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bookmark(@PathVariable Long id) {
        postService.bookmark(CurrentUser.id(), id);
    }

    @DeleteMapping("/{id}/bookmark")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unbookmark(@PathVariable Long id) {
        postService.unbookmark(CurrentUser.id(), id);
    }

    @GetMapping("/{id}/comments")
    public PageDto<CommentDto> comments(@PathVariable Long id, @RequestParam(defaultValue = "0") int page) {
        return postService.comments(CurrentUser.id(), id, page);
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable Long id, @Valid @RequestBody CreateCommentRequest req) {
        return postService.addComment(CurrentUser.id(), id, req);
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long id, @PathVariable Long commentId) {
        postService.deleteComment(CurrentUser.id(), id, commentId);
    }
}
