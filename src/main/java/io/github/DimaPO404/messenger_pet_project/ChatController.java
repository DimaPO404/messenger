package io.github.DimaPO404.messenger_pet_project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage message) {
        if (message.content() == null || message.content().isBlank()) {
            throw new IllegalArgumentException("Текст сообщения не может быть пустым");
        }

        ChatMessage responseMessage = new ChatMessage(
                null,
                message.sender(),
                message.recipient(),
                message.content(),
                message.chatId(),
                LocalDateTime.now(),
                MessageStatus.SENT
        );

        String destination = "/topic/chat/" + message.chatId();
        messagingTemplate.convertAndSend(destination, responseMessage);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage message) {
        ChatMessage joinMessage = new ChatMessage(
                null,
                "System",
                message.sender(),
                message.sender() + " присоединился к чату",
                message.chatId(),
                LocalDateTime.now(),
                MessageStatus.SENT
        );

        String destination = "/topic/chat/" + message.chatId();
        messagingTemplate.convertAndSend(destination, joinMessage);
    }
}