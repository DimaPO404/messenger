package io.github.DimaPO404.messenger_pet_project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<ChatMessage, Long> {
    Optional<ChatMessage> findById(Long messageId);
}
