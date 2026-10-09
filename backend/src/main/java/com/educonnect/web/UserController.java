package com.educonnect.web;

import com.educonnect.security.CurrentUser;
import com.educonnect.service.PostService;
import com.educonnect.service.UserService;
import com.educonnect.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PostService postService;

    public UserController(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    @GetMapping("/me")
    public UserDto me() {
        Long me = CurrentUser.id();
        return userService.get(me, me);
    }

    @PutMapping("/me")
    public UserDto updateMe(@Valid @RequestBody UpdateProfileRequest req) {
        return userService.updateProfile(CurrentUser.id(), req);
    }

    @GetMapping("/me/bookmarks")
    public PageDto<PostDto> bookmarks(@RequestParam(defaultValue = "0") int page) {
        return postService.bookmarks(CurrentUser.id(), page);
    }

    @GetMapping
    public List<UserSummaryDto> search(@RequestParam(defaultValue = "") String q) {
        return userService.search(q);
    }

    @GetMapping("/{id}")
    public UserDto get(@PathVariable Long id) {
        return userService.get(CurrentUser.id(), id);
    }

    @GetMapping("/{id}/posts")
    public PageDto<PostDto> posts(@PathVariable Long id, @RequestParam(defaultValue = "0") int page) {
        return postService.byAuthor(CurrentUser.id(), id, page);
    }

    @PostMapping("/{id}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void follow(@PathVariable Long id) {
        userService.follow(CurrentUser.id(), id);
    }

    @DeleteMapping("/{id}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollow(@PathVariable Long id) {
        userService.unfollow(CurrentUser.id(), id);
    }
}
