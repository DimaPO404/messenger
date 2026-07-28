package io.github.DimaPO404.messenger_pet_project.chat;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    Optional<Chat> findByInviteCode(String inviteCode);
}
