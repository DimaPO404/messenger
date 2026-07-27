package io.github.DimaPO404.messenger_pet_project.chat;

import io.github.DimaPO404.messenger_pet_project.user.User;
import io.github.DimaPO404.messenger_pet_project.user.UserRepository;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {
    private final Logger log = LoggerFactory.getLogger(ChatService.class);

    private MessageRepository messageRepository;
    private UserRepository userRepository;

    public ChatService(MessageRepository repository, UserRepository userRepository) {
        this.messageRepository = repository;
        this.userRepository = userRepository;
    }

    public ChatMessage processAndSaveMessage(ChatMessage message) {
        log.info("Попытка отправить сообщение");

        if (message.getContent() == null || message.getContent().isBlank()) {
            log.warn("Попытка отправить пустое сообщение");
            throw new IllegalArgumentException("Текст сообщения не может быть пустым");
        }

        message.setTimeStamp(LocalDateTime.now());
        message.setStatus(MessageStatus.SENT);

        log.info("Сообщение успешно отправлено");
        return messageRepository.save(message);
    }

    public ChatMessage addNewUserAndSaveMessage(ChatMessage message) {
        log.info("Попытка нового пользователя зайти в чат: {}", message.getSender());

        User user = userRepository.findByUserId(message.getSender());
        String userName = (user != null) ? user.getName() : "Неизвестный пользователь";

        log.warn("Имя пользователя было успешно найдено в базе данных, попытка отправить приветственное сообщение");
        message.setSender("System");
        message.setContent(userName + " присоединился к чату");
        message.setRecipient("all");
        message.setTimeStamp(LocalDateTime.now());
        message.setStatus(MessageStatus.SENT);

        log.info("Новый пользователь успешно присоединился к чату ");
        return messageRepository.save(message);
    }

    public ChatMessage deleteUserAndSaveMessage(ChatMessage message) {
        log.info("Попытка пользователя выйти из чата: {}", message.getSender());

        User user = userRepository.findByUserId(message.getSender());
        String userName = (user != null) ? user.getName() : "Неизвестный пользователь";

        log.warn("Имя пользователя было успешно найдено в базе данных, попытка отправить сообщение о выходе");
        message.setSender("System");
        message.setContent(userName + " покинул чат");
        message.setRecipient("all");
        message.setTimeStamp(LocalDateTime.now());
        message.setStatus(MessageStatus.SENT);

        log.info("Пользователь успешно вышел из чата");
        return messageRepository.save(message);
    }

    @Transactional
    public ChatMessage deleteMessage(Long messageId, String userId) {
        log.info("Попытка удалить своё сообщение: {}", messageId);

        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Сообщение не найдено"));

        if (!message.getSender().equals(userId)) {
            log.warn("Попытка удалить чужое сообщение: {}", messageId);
            throw new IllegalArgumentException("Нельзя удалить чужое сообщение");
        }

        log.info("Сообщение успешно удалено: {}", messageId);
        message.setStatus(MessageStatus.DELETED);
        return messageRepository.save(message);
    }

    @Transactional
    public ChatMessage editMyMessage(Long messageId, String userId, String newContent) {
        log.info("Попытка редактировать сообщение: {}", messageId);

        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Сообщение не найдено"));

        if (!message.getSender().equals(userId)) {
            log.warn("Попытка редактировать чужое сообщение: {}", messageId);
            throw new IllegalArgumentException("Нельзя изменить чужое сообщение");
        }

        message.setContent(newContent);
        message.setTimeStamp(LocalDateTime.now());

        log.info("Сообщение успешно отредактировано: {}", messageId);
        return messageRepository.save(message);
    }

    public Chat createChat(CreateChatRequest request, String creatorUserId) {
        Chat chat = new Chat();
        chat.setName(request.getName());
        chat.setType(request.getType());
        chat.setInviteCode(generativeCode());
        chat.createdBy(creatorUserId);

        Chat savedChat = chatRepository.save(chat);

        addUserToChat(savedChat.getId(), creatorUserId);

        for (String userId : request.getUserIds()) {
            addUserToChat(savedChat.getId(), userId);
        }

        return savedChat;
    }
}
