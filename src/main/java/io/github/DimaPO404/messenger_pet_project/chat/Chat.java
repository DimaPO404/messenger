package io.github.DimaPO404.messenger_pet_project.chat;

import jakarta.persistence.*;
import org.hibernate.annotations.Audited;
import org.springframework.data.annotation.Id;

@Entity
@Audited.Table(name = "chats")
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
}
