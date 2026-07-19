package io.github.DimaPO404.messenger_pet_project;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User createUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            throw new IllegalArgumentException("User name cannot be empty");
        }
        if (user.getPassword().length() <= 4) {
            throw new IllegalArgumentException("Password length should be more than 4 symbols");
        }
        if (!user.getUserId().matches("^@[a-zA-Z0-9_]{1,30}$")) {
            throw new IllegalArgumentException("invalid userID format");
        }
        if (repository.existsByUserId(user.getUserId())) {
            throw new IllegalArgumentException("This username is already taken");
        }
        if (!user.getPhone().matches("^\\+?\\d[\\d\\s\\-\\(\\)]{6,14}\\d$")) {
            throw new IllegalArgumentException("invalid phone format");
        }

        user.setCreatedAt(LocalDateTime.now());

        repository.save(user);
        return user;
    }
}
