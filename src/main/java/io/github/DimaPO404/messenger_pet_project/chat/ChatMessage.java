package io.github.DimaPO404.messenger_pet_project.chat;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Table(name = "chat_messages")
@Entity
public class ChatMessage {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "sender", nullable = false)
    private String sender;
    @Column(name = "recipient", nullable = false)
    private String recipient;
    @Column(name = "content", nullable = false)
    private String content;
    @Column(name = "chat_id", nullable = false)
    private Long chatId;
    @Column(name = "time_stamp", nullable = false)
    private LocalDateTime timeStamp;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageStatus status;

    public ChatMessage() {
    }

    public ChatMessage(Long id, String sender, String recipient, String content, Long chatId, LocalDateTime timeStamp, MessageStatus status) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.chatId = chatId;
        this.timeStamp = timeStamp;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getContent() {
        return content;
    }

    public Long getChatId() {
        return chatId;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }
}
