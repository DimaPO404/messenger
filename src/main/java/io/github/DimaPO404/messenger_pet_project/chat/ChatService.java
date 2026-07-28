package io.github.DimaPO404.messenger_pet_project.chat;

import io.github.DimaPO404.messenger_pet_project.user.User;
import io.github.DimaPO404.messenger_pet_project.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final ChatUserRepository chatUserRepository;

    public ChatService(MessageRepository messageRepository,
                       UserRepository userRepository,
                       ChatRepository chatRepository,
                       ChatUserRepository chatUserRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.chatUserRepository = chatUserRepository;
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

    @Transactional
    public Chat createChat(CreateChatRequest request, String creatorUserId) {
        log.info("Создание чата пользователем: {}", creatorUserId);

        if (request.getType() == ChatType.PERSONAL && request.getUserIds() != null && request.getUserIds().size() == 1) {
            Chat existingChat = findPersonalChat(creatorUserId, request.getUserIds().get(0));
            if (existingChat != null) {
                log.info("Личный чат уже существует, возвращаем его: {}", existingChat.getId());
                return existingChat;
            }
        }

        Chat chat = new Chat();
        chat.setName(request.getName());
        chat.setType(request.getType());

        if (request.getType() == ChatType.GROUP) {
            chat.setInviteCode(generateInviteCode());
        }

        chat.setCreatedBy(creatorUserId);

        Chat savedChat = chatRepository.save(chat);
        log.info("Чат создан с ID: {}", savedChat.getId());

        addUserToChat(savedChat, creatorUserId);

        if (request.getUserIds() != null) {
            for (String userId : request.getUserIds()) {
                if (!userId.equals(creatorUserId)) {
                    addUserToChat(savedChat, userId);
                }
            }
        }

        return savedChat;
    }

    private Chat findPersonalChat(String userId1, String userId2) {
        User user1 = userRepository.findByUserId(userId1);
        User user2 = userRepository.findByUserId(userId2);

        if (user1 == null || user2 == null) return null;

        List<ChatUser> user1Chats = chatUserRepository.findByUserId(user1.getId());
        List<ChatUser> user2Chats = chatUserRepository.findByUserId(user2.getId());

        for (ChatUser cu1 : user1Chats) {
            for (ChatUser cu2 : user2Chats) {
                if (cu1.getChat().getId().equals(cu2.getChat().getId())
                        && cu1.getChat().getType() == ChatType.PERSONAL) {
                    return cu1.getChat();
                }
            }
        }
        return null;
    }

    @Transactional
    public Chat joinChatByInviteCode(String inviteCode, String userId) {
        log.info("Попытка присоединиться к чату по коду: {}", inviteCode);

        Chat chat = chatRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("Чат с таким кодом не найден"));

        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден");
        }

        List<ChatUser> existingMembers = chatUserRepository.findByChatId(chat.getId());
        boolean alreadyMember = existingMembers.stream()
                .anyMatch(cu -> cu.getUser().getId().equals(user.getId()));

        if (alreadyMember) {
            log.warn("Пользователь {} уже состоит в чате", userId);
            return chat;
        }

        addUserToChat(chat, userId);
        log.info("Пользователь {} присоединился к чату {}", userId, chat.getId());

        return chat;
    }

    public List<Chat> getUserChats(String userId) {
        log.info("Получение списка чатов для пользователя: {}", userId);

        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден");
        }

        List<ChatUser> chatUsers = chatUserRepository.findByUserId(user.getId());

        return chatUsers.stream()
                .map(ChatUser::getChat)
                .collect(Collectors.toList());
    }

    private String generateInviteCode() {
        return UUID.randomUUID().toString();
    }

    private void addUserToChat(Chat chat, String userId) {
        User user = userRepository.findByUserId(userId);

        if (user != null) {
            ChatUser chatUser = new ChatUser();
            chatUser.setChat(chat);
            chatUser.setUser(user);

            chatUserRepository.save(chatUser);
            log.info("Пользователь {} добавлен в чат {}", userId, chat.getId());
        } else {
            log.warn("Пользователь {} не найден, не добавлен в чат", userId);
        }
    }
}