package io.github.DimaPO404.messenger_pet_project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    private SimpMessagingTemplate messagingTemplate;
    private MessageRepository repository;
    private final ChatService chatService;

    @Autowired
    public ChatController(SimpMessagingTemplate messagingTemplate, MessageRepository repository, ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.repository = repository;
        this.chatService = chatService;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage message) {
        ChatMessage savedMessage = chatService.processAndSaveMessage(message);

        String destination = "/topic/chat/" + savedMessage.getChatId();
        messagingTemplate.convertAndSend(destination, savedMessage);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage message) {
        ChatMessage savedMessage = chatService.addNewUserAndSaveMessage(message);

        String destination = "/topic/chat/" + savedMessage.getChatId();
        messagingTemplate.convertAndSend(destination, savedMessage);
    }

    @MessageMapping("/chat.deleteUser")
    public void deleteUser(@Payload ChatMessage message) {
        ChatMessage savedMessage = chatService.deleteUserAndSaveMessage(message);

        String destination = "/topic/chat/" + message.getChatId();
        messagingTemplate.convertAndSend(destination, savedMessage);
    }

    @MessageMapping("/chat.deleteMessage")
    @Transactional
    public void deleteMessage(@Payload ChatMessage message) {
        ChatMessage deleteMessage = chatService.deleteMessage(
                message.getId(),
                message.getSender()
        );

        String destination = "/topic/chat/" + message.getChatId();
        messagingTemplate.convertAndSend(destination, deleteMessage);
    }

    @MessageMapping("/chat.editMessage")
    @Transactional
    public void editMessage(@Payload ChatMessage message) {
        ChatMessage editMessage = chatService.editMyMessage(
                message.getId(),
                message.getSender(),
                message.getContent()
        );

        String destination = "/topic/chat/" + message.getChatId();
        messagingTemplate.convertAndSend(destination, editMessage);
    }
}