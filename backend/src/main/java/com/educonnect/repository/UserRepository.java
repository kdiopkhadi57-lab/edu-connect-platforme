package com.educonnect.repository;

import com.educonnect.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("select u from User u join u.communities c where c.id = :communityId order by u.fullName")
    Page<User> findMembers(@Param("communityId") Long communityId, Pageable pageable);

    @Query("select count(u) from User u join u.communities c where c.id = :communityId")
    long countMembers(@Param("communityId") Long communityId);

    @Query("""
            select u from User u
            where lower(u.fullName) like lower(concat('%', :q, '%'))
               or lower(u.institution) like lower(concat('%', :q, '%'))
            order by u.fullName""")
    Page<User> search(@Param("q") String q, Pageable pageable);
}
