package io.github.DimaPO404.messenger_pet_project;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Table(name = "users")
@Entity
public class User {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "phone", nullable = false)
    private String phone;
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;
    @Column(name = "description", nullable = true)
    private String description;
    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    public User() {
    }

    public User(Long id, String name, String password, String phone, String userId, String description, LocalDateTime createAt) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.phone = phone;
        this.userId = userId;
        this.description = description;
        this.createdAt = createAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public String getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedAt(LocalDateTime createAt) {
        this.createdAt = createAt;
    }
}
