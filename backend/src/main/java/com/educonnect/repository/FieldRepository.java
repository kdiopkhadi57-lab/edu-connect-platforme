package com.educonnect.repository;

import com.educonnect.domain.Field;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FieldRepository extends JpaRepository<Field, Long> {

    List<Field> findAllByOrderByNameAsc();

    Optional<Field> findByName(String name);
}
