package io.github.DimaPO404.messenger_pet_project.chat;

import jakarta.persistence.*;

@Entity
@Table(name = "chats")  // Исправлено: было @Audited.Table
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ChatType type;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "invite_code")
    private String inviteCode;

    @Column(name = "created_by")  // ДОБАВЛЕНО: поле кто создал чат
    private String createdBy;

    // Геттеры
    public Long getId() {
        return id;
    }

    public ChatType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    // ДОБАВЛЕНО: геттер для createdBy
    public String getCreatedBy() {
        return createdBy;
    }

    // Сеттеры
    public void setId(Long id) {
        this.id = id;
    }

    public void setType(ChatType type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    // ДОБАВЛЕНО: сеттер для createdBy
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}