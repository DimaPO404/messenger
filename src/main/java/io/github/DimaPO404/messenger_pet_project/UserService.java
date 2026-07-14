package io.github.DimaPO404.messenger_pet_project;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    List<User> users = new ArrayList<>();

    public User createUser(User user) {
        if (user.name() == null || user.name().isBlank()) {
            throw new IllegalArgumentException("User name cannot be empty");
        }
        if (user.password().length() <= 4) {
            throw new IllegalArgumentException("Password length should be more than 4 symbols");
        }
        if (!user.userId().matches("^@[a-zA-Z0-9_]{1,30}$")) {
            throw new IllegalArgumentException("invalid userID format");
        }
        for (User element : users) {
                if (element.userId().equals(user.userId()))
                    throw new IllegalArgumentException("This username is already taken");
        }
        if (!user.phone().matches("^\\+?\\d[\\d\\s\\-\\(\\)]{6,14}\\d$")) {
            throw new IllegalArgumentException("invalid phone format");
        }

        User newUser = new User(
                null,
                user.name(),
                user.password(),
                user.phone(),
                user.userId(),
                user.description(),
                LocalDateTime.now()
        );

        users.add(newUser);
        return newUser;
    }
}
