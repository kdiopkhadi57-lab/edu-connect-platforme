package com.educonnect.repository;

import com.educonnect.domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByPostIdAndUserId(Long postId, Long userId);

    @Query("select b.post.id from Bookmark b where b.user.id = :userId and b.post.id in :postIds")
    Set<Long> findBookmarkedPostIds(@Param("userId") Long userId, @Param("postIds") Collection<Long> postIds);

    @Modifying
    @Query("delete from Bookmark b where b.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
