package com.educonnect.repository;

import com.educonnect.domain.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerIdAndFollowedId(Long followerId, Long followedId);

    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);

    @Query("select f.followed.id from Follow f where f.follower.id = :userId")
    List<Long> findFollowedIds(@Param("userId") Long userId);

    long countByFollowerId(Long followerId);

    long countByFollowedId(Long followedId);
}
