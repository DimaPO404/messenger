package io.github.DimaPO404.messenger_pet_project.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatUserRepository extends JpaRepository<ChatUser, Long> {
    List<ChatUser> findByUserId(Long userId);

    List<ChatUser> findByChatId(Long chatId);
}