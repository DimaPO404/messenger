package io.github.DimaPO404.messenger_pet_project.chat;

import io.github.DimaPO404.messenger_pet_project.user.User;
import jakarta.persistence.*;
import jakarta.persistence.Id;

@Entity
public class ChatUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @Column(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne
    @Column(name = "user_id", nullable = false)
    private User user;

    public Long getId() {
        return id;
    }

    public Chat getChat() {
        return chat;
    }

    public User getUser() {
        return user;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
