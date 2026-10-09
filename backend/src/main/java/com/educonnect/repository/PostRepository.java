package com.educonnect.repository;

import com.educonnect.domain.Field;
import com.educonnect.domain.Level;
import com.educonnect.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    /**
     * Fil « Pour toi » : publications de mes communautés, de mon domaine/niveau,
     * des personnes que je suis, et les miennes.
     */
    @Query("""
            select p from Post p
            where p.community.id in :communityIds
               or (p.field = :field and p.level = :level)
               or p.author.id in :authorIds
            order by p.createdAt desc""")
    Page<Post> findFeed(@Param("communityIds") Collection<Long> communityIds,
                        @Param("field") Field field,
                        @Param("level") Level level,
                        @Param("authorIds") Collection<Long> authorIds,
                        Pageable pageable);

    Page<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    Page<Post> findByCommunityIdOrderByCreatedAtDesc(Long communityId, Pageable pageable);

    @Query("select b.post from Bookmark b where b.user.id = :userId order by b.createdAt desc")
    Page<Post> findBookmarked(@Param("userId") Long userId, Pageable pageable);

    long countByAuthorId(Long authorId);

    @Modifying
    @Query("update Post p set p.likeCount = p.likeCount + :delta where p.id = :id")
    void addLikes(@Param("id") Long id, @Param("delta") int delta);

    @Modifying
    @Query("update Post p set p.commentCount = p.commentCount + :delta where p.id = :id")
    void addComments(@Param("id") Long id, @Param("delta") int delta);

    @Modifying
    @Query("update Post p set p.downloadCount = p.downloadCount + 1 where p.id = :id")
    void incrementDownloads(@Param("id") Long id);
}
