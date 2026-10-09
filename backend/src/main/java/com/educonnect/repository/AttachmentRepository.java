package com.educonnect.repository;

import com.educonnect.domain.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    Optional<Attachment> findByStoredName(String storedName);
}
