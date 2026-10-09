package com.educonnect.web;

import com.educonnect.security.CurrentUser;
import com.educonnect.service.CommunityService;
import com.educonnect.service.PostService;
import com.educonnect.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/communities")
public class CommunityController {

    private final CommunityService communityService;
    private final PostService postService;

    public CommunityController(CommunityService communityService, PostService postService) {
        this.communityService = communityService;
        this.postService = postService;
    }

    @GetMapping("/mine")
    public List<CommunityDto> mine() {
        return communityService.mine(CurrentUser.id());
    }

    @GetMapping
    public List<CommunityDto> search(@RequestParam(defaultValue = "") String q) {
        return communityService.search(CurrentUser.id(), q);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommunityDto create(@Valid @RequestBody CreateCommunityRequest req) {
        return communityService.create(CurrentUser.id(), req);
    }

    @GetMapping("/{id}")
    public CommunityDto get(@PathVariable Long id) {
        return communityService.get(CurrentUser.id(), id);
    }

    @GetMapping("/{id}/posts")
    public PageDto<PostDto> posts(@PathVariable Long id, @RequestParam(defaultValue = "0") int page) {
        return postService.byCommunity(CurrentUser.id(), id, page);
    }

    @GetMapping("/{id}/members")
    public PageDto<UserSummaryDto> members(@PathVariable Long id, @RequestParam(defaultValue = "0") int page) {
        return communityService.members(id, page);
    }

    @PostMapping("/{id}/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void join(@PathVariable Long id) {
        communityService.join(CurrentUser.id(), id);
    }

    @DeleteMapping("/{id}/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(@PathVariable Long id) {
        communityService.leave(CurrentUser.id(), id);
    }
}
