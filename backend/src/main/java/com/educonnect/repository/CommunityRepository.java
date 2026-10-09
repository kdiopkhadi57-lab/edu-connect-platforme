package com.educonnect.repository;

import com.educonnect.domain.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    Optional<Community> findBySlug(String slug);

    @Query("select c from Community c where lower(c.name) like lower(concat('%', :q, '%')) order by c.name")
    Page<Community> search(@Param("q") String q, Pageable pageable);
}
