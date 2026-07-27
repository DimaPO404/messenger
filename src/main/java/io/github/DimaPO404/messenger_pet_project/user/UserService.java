package io.github.DimaPO404.messenger_pet_project.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserService {
    private final UserRepository repository;
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User createUser(User user) {
        log.info("Попытка регистрации пользователя: {}", user.getUserId());

        if (user.getName() == null || user.getName().isBlank()) {
            log.warn("Пустое имя при регистрации: {}", user.getName());
            throw new IllegalArgumentException("User name cannot be empty");
        }
        if (user.getPassword().length() <= 4) {
            log.warn("Слишком короткий пароль");
            throw new IllegalArgumentException("Password length should be more than 4 symbols");
        }
        if (!user.getUserId().matches("^@[a-zA-Z0-9_]{1,30}$")) {
            log.warn("Некорректный формат userId: {}", user.getUserId());
            throw new IllegalArgumentException("invalid userID format");
        }
        if (repository.existsByUserId(user.getUserId())) {
            log.warn("Попытка зарегистрировать существующий userId: {}", user.getUserId());
            throw new IllegalArgumentException("This username is already taken");
        }
        if (!user.getPhone().matches("^\\+?\\d[\\d\\s\\-\\(\\)]{6,14}\\d$")) {
            log.warn("Некорректный формат номера телефона: {}", user.getPhone());
            throw new IllegalArgumentException("invalid phone format");
        }

        user.setCreatedAt(LocalDateTime.now());

        log.info("Пользователь успешно зарегистрирован с id={}", user.getId());
        return repository.save(user);
    }

    public User loginUser(String userId, String password) {
        log.info("Попытка зайти в аккаунт с userId {}", userId);

        if (!repository.existsByUserId(userId)) {
            log.warn("Введён несуществующий userId: {}", userId);
            throw new IllegalArgumentException("This username ID was not found");
        }

        User loginUser = repository.findByUserId(userId);

        if (!password.equals(loginUser.getPassword())) {
            log.warn("Неправильный пароль");
            throw new IllegalArgumentException("Wrong password");
        }

        log.info("Успешный вход в аккаунт: {}", userId);
        return loginUser;
    }
}
